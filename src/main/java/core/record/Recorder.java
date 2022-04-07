package core.record;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Slf4j
public class Recorder {

    private ReentrantReadWriteLock lock;
    private File file;

    public Recorder(String filePath, String fileName) {
        this.lock = new ReentrantReadWriteLock();
        this.file = new File(filePath, fileName);
        File fileParent = this.file.getParentFile();
        try {
            if (!fileParent.exists())
                fileParent.mkdirs();
            if (!this.file.exists())
                this.file.createNewFile();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public Exception RecordHistory(Operation<?> operation) {
        try {
            String json = JSON.toJSONStringWithDateFormat(operation, "yyyy-MM-dd HH:mm:ss.SSS");
            this.lock.writeLock().lock();
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.file, true));        // 追加写入
            bw.write(json);
            bw.newLine();
            bw.flush();
            bw.close();
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return e;
        } finally {
            try {
                this.lock.writeLock().unlock();
            } catch (IllegalMonitorStateException e) {
                log.warn("Error before lock, object to json wrong.");
            }
        }
    }
}
