package frc.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Timer;
//import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
//import com.ctre.phoenix.motorcontrol.can.BasePIDSetConfiguration;

import com.kauailabs.navx.frc.AHRS;

class WestCoastDrive {
    private final double kP                            = 0.06d;
    private final double kI                            = 0.0d;
    private final double kD                            = 0.0d;
    private AHRS navX;
    // By seconds
    //private final Timer autonTimer;

    private final ADXRS450_Gyro gyro                  = new ADXRS450_Gyro( );
    private final PIDController anglePID              = new PIDController(kP, kI, kD);
    private final WPI_TalonFX _rghtMain               = new WPI_TalonFX(RobotMap._rghtMain);
    private final WPI_TalonFX _rghtFol1               = new WPI_TalonFX(RobotMap._rghtFol1);
    private final WPI_TalonFX _leftMain               = new WPI_TalonFX(RobotMap._leftMain);
    private final WPI_TalonFX _leftFol1               = new WPI_TalonFX(RobotMap._leftFol1);

    private double rotRate = 0.0d;

    private Joystick driveJoy;

    
    private final DifferentialDrive mainDrive = new DifferentialDrive(_leftMain, _rghtMain);

    public WestCoastDrive( Timer autonTimer, Joystick driveJoy) {
        try{
            navX = new AHRS(SPI.Port.kMXP);
        }catch(RuntimeException e){
            e.printStackTrace();
        }


        this.driveJoy = driveJoy;

        //this.autonTimer = autonTimer;
        _rghtFol1.follow( _rghtMain  );
        _leftFol1.follow( _leftMain  );
    
        _rghtMain.setInverted( false  );
        _leftMain.setInverted( false );
        _rghtMain.configClosedloopRamp(2);
        _leftMain.configClosedloopRamp(2);
        anglePID.setTolerance( 1.0d );
        _rghtMain.getSensorCollection().setIntegratedSensorPosition(0.0, 0);

        gyro.calibrate();
     }


    public void autonomousInit() {
        _rghtMain.getSensorCollection().setIntegratedSensorPosition(0.0, 0);
        _rghtMain.setNeutralMode(NeutralMode.Brake);
        _leftMain.setNeutralMode(NeutralMode.Brake);
        navX.reset();
        anglePID.reset();
    }
    public void autonomousPeriodic() {

        if(navX.getAngle() >= 2){
            rotRate = -0.35;
        }else if(navX.getAngle() <= -2){
            rotRate = 0.35;
        }else if(navX.getAngle() >= 1 ) {
            rotRate = -.2; 
        }else if(navX.getAngle() <= -1) {
            rotRate = .2;
                        
        }else {
            rotRate = 0;
        }
        SmartDashboard.putNumber("Rotation: ", rotRate);
        
        SmartDashboard.putNumber("Drive Enc: ",_rghtMain.getSelectedSensorPosition() );
            if(_rghtMain.getSelectedSensorPosition() >= -150000){
                arcadeDrive(0.46, rotRate);
                SmartDashboard.putString("Driving: ", "Driving");
            }else{
                SmartDashboard.putString("Driving: ", "DEAD!");
                arcadeDrive(0.0, 0.0);
            }

    }
    public void teleopInit() {
        navX.zeroYaw();
    }
    public void teleopPeriodic() {

        SmartDashboard.putNumber("Gyro Reaing", navX.getAngle());

        if(driveJoy.getRawButton(2)){

            if(driveJoy.getRawButton(1)){
                if(InputMap.DeadBand(0.2d, driveJoy.getRawAxis(InputMap.DRIVEJOY_Y)) ||
                    InputMap.DeadBand(0.25d, driveJoy.getRawAxis(InputMap.DRIVEJOY_Z))){
                    arcadeDrive(driveJoy.getRawAxis(InputMap.DRIVEJOY_Y) * (InputMap.SPEED_Y - 0.15),
                    driveJoy.getRawAxis(InputMap.DRIVEJOY_Z) * (InputMap.SPEED_Z));
                }
            }else{
                if(InputMap.DeadBand(0.2d, driveJoy.getRawAxis(InputMap.DRIVEJOY_Y)) ||
                InputMap.DeadBand(0.25d, driveJoy.getRawAxis(InputMap.DRIVEJOY_Z))){
                    arcadeDrive(driveJoy.getRawAxis(InputMap.DRIVEJOY_Y) * InputMap.SPEED_Y,
                    driveJoy.getRawAxis(InputMap.DRIVEJOY_Z) * InputMap.SPEED_Z - 0.15);
                }
            }

        }else{
            if(driveJoy.getRawButton(1)){
                if(InputMap.DeadBand(0.2d, driveJoy.getRawAxis(InputMap.DRIVEJOY_Y)) ||
                    InputMap.DeadBand(0.25d, driveJoy.getRawAxis(InputMap.DRIVEJOY_Z))){
                    arcadeDrive(-driveJoy.getRawAxis(InputMap.DRIVEJOY_Y) * (InputMap.SPEED_Y - 0.15),
                    driveJoy.getRawAxis(InputMap.DRIVEJOY_Z) * (InputMap.SPEED_Z - 0.15));
                }
            }else{
                if(InputMap.DeadBand(0.2d, driveJoy.getRawAxis(InputMap.DRIVEJOY_Y)) ||
                InputMap.DeadBand(0.25d, driveJoy.getRawAxis(InputMap.DRIVEJOY_Z))){
                    arcadeDrive(-driveJoy.getRawAxis(InputMap.DRIVEJOY_Y) * InputMap.SPEED_Y,
                    driveJoy.getRawAxis(InputMap.DRIVEJOY_Z) * InputMap.SPEED_Z);
                }
            }
        }
    }
    
    /*
     *
     * This function accepts joystick input to move the robot.
     */
    public void arcadeDrive( final double y, final double x) {
        mainDrive.arcadeDrive( y, x);
    }
    

    Timer tempTimer = new Timer();

    public void disabledInit(){
        _rghtMain.setNeutralMode(NeutralMode.Coast);
        _leftMain.setNeutralMode(NeutralMode.Coast);

    }

    public void testInit(){
        tempTimer.reset();
        tempTimer.start();
        anglePID.reset();
        navX.reset();
        anglePID.setSetpoint(0.0);
    }

    public void testing(){

        SmartDashboard.putNumber("Gyro Reading: ", navX.getAngle());
        double rotRate = anglePID.calculate(Math.round(navX.getAngle()));

        /*if(rotRate > 0.4){
            rotRate = 0.4;
        }else if(rotRate < -0.4){
            rotRate = -0.4;
        }*/

        if(tempTimer.get() <= 6){
            arcadeDrive(.45, rotRate);
        }else{
            arcadeDrive(0, 0);
        }


    }
}