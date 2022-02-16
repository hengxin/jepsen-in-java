package core.nemesis;

public class NemesisControl {

    private int signal;     // NemesisControl is used to operate nemesis between the control side and test client side

    // WaitForStart is used on control side to wait for enabling start nemesis
    public void WaitForStart(){

    }

    // WaitForRollback is used on control side to wait for enabling rollback nemesis
    public void WaitForRollBack(){

    }

    // Start is used on client side to enable control side starting nemesis
    public void Start(){

    }

    // Rollback is used on client side to enable control side rollbacking nemesis
    public void RollBack(){

    }
}
