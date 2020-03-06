package frc.robot;
//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Joystick;

 class Limelight {
  private Joystick    MINIPJOY_1;
  private Joystick    MINIPJOY_2;
  private BallShooter _turret;

  public Limelight( Joystick MINIPJOY_1, Joystick MINIPJOY_2, BallShooter _turret ) {
    this.MINIPJOY_1 = MINIPJOY_1;
    this.MINIPJOY_2 = MINIPJOY_2;
    this._turret = _turret;
  }
  private boolean m_LimelightHasValidTarget = false;

  private double m_LimelightSteerCommand = 0.0;
  

  public void teleopPeriodic() {

    Update_Limelight_Tracking();

        
          //if (m_Controller.getRawButton(1)){

          
            NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(0);
            if (m_LimelightHasValidTarget){
          
                _turret.turretTurn(-m_LimelightSteerCommand);
                
          }
          else
          {
            _turret.turretTurn(0.0);
           }
  }




  

 
  public void Update_Limelight_Tracking()
  {
        // These numbers must be tuned for your Robot!  Be careful!
        final double TURN_K = 0.0225;                     // how hard to turn toward the target
        final double RIGHT_MAX = 0.4;                   // Max speed the turret motor can go
        final double LEFT_MAX = -0.4;
        final double RIGHT_MIN = 0.03;
        final double LEFT_MIN = -0.03;

        double tv = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);
        double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);

        
        if (tv < 1.0)
        {
          m_LimelightHasValidTarget = false;
          m_LimelightSteerCommand = 0.0;
          return;
        }

        m_LimelightHasValidTarget = true;

        // Start with proportional steering
        double turn_cmd = tx * TURN_K;
        if (turn_cmd > RIGHT_MAX)
        {
          turn_cmd = RIGHT_MAX;
        }else if(turn_cmd > 0 && turn_cmd < RIGHT_MIN){
          turn_cmd = RIGHT_MIN;
        }else if(turn_cmd < 0 && turn_cmd > LEFT_MIN){
          turn_cmd = LEFT_MIN;
        } else if (turn_cmd < LEFT_MAX){
          turn_cmd = LEFT_MAX;
        }

        m_LimelightSteerCommand = turn_cmd;
        System.out.println(turn_cmd);

  }

}