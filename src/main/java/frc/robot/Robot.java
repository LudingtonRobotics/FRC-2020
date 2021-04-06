/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------*/
/* Team 7160, Ludington Obots                                                 */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Robot extends TimedRobot {
  
    //------WPISYSTEMS------// 
    private final Timer autonTimer = new Timer();
    private final Joystick DRIVEJOY = new Joystick(InputMap.DRIVEJOY);
    private final Joystick MINIPJOY_1 = new Joystick(InputMap.MINIPJOY_1);
    private final Joystick MINIPJOY_2 = new Joystick(InputMap.MINIPJOY_2);
    private DriverStation driverStation = DriverStation.getInstance();
    //----------------------//

    //------SUBSYSTEMS------//
    private BallShooter shooter;
    private BallHandler ballHandler;
    private Spinner spinner;
    private Limelight limeLight;
    private PhotonVision photonVision;
    private WestCoastDrive _drive;
    private Led LEDS = new Led();
    private Lift_Leveler liftLeveler;
    //----------------------//    

    public static enum AutoModes{
       SHOOT, FORWARD, INDEX, BACKWARD
    }

    public static AutoModes mode;
    private Timer pathTimer = new Timer();

    public static AutoModes getAutoModes(){
      return mode;
    }

    private enum galaticSearch{
     
      DETERMINECOLOR, // Used to determine path color

       DETERMINEPATH, // Used to determine path letter

        REDPATH_A_BALL2, // Turn and go to ball 2
         REDPATH_A_BALL3, // Turn and go to ball 3
          REDPATH_A_END, // Drive to finish
        // All others do same for respective values
        BLUEPATH_A_BALL2,
          BLUEPATH_A_BALL3,
           BLUEPATH_A_END,

        REDPATH_B_BALL2,
          REDPATH_B_BALL3,
            REDPATH_B_END,

        BLUEPATH_B_BALL2,
          BLUEPATH_B_BALL3,
            BLUEPATH_B_END,

    }

    private galaticSearch galaticMode = galaticSearch.DETERMINECOLOR;

    @Override
    public void robotInit() {
      shooter = new BallShooter(autonTimer , MINIPJOY_1, MINIPJOY_2, DRIVEJOY);
      spinner = new Spinner( LEDS , MINIPJOY_1, MINIPJOY_2 );
      limeLight = new Limelight( DRIVEJOY, MINIPJOY_1, MINIPJOY_2, shooter );
      ballHandler = new BallHandler(autonTimer, MINIPJOY_1, MINIPJOY_2, DRIVEJOY);
      _drive = new WestCoastDrive( autonTimer, DRIVEJOY );
      liftLeveler = new Lift_Leveler(driverStation, MINIPJOY_2, DRIVEJOY);
      photonVision = new PhotonVision(_drive);
      
    }

    @Override
    public void robotPeriodic() {
      ballHandler.robotPeriodic();
    }

    @Override
    public void autonomousInit() {
      galaticMode = galaticSearch.DETERMINECOLOR;
      _drive.autonomousInit();
      red = false;
      blue = false;
      ballHandler.galaticReset();
      pathTimer.start();
      pathTimer.reset();
      timerReset = false;
      hasReset = false;
      
    }


    // Distance Error 9192
    boolean red = false;
    boolean blue = false;
    boolean timerReset = false;

    boolean driveDone = false;
    boolean driveForward = false;
    boolean hasReset = false;

    private boolean driveToDistance(double driveDistance){
      if(!hasReset){
        _drive.resetRightMotor();
        _drive.anglePID.setSetpoint(0);
        _drive.navX.reset();
        hasReset = true;
      }else{
        if(_drive.getDistance() > driveDistance){
          _drive.arcadeDrive(.45, _drive.anglePID.calculate(_drive.navX.getAngle()));
        }else{
          _drive.arcadeDrive(0, 0);
          return true;
        }
      }
      return false;
    }

    // -74491

    @Override
    public void autonomousPeriodic(){ 
      photonVision.updatePhoton();
      ballHandler.getball();
       
      switch(galaticMode){
        case DETERMINECOLOR:

        

        if(!blue){
          if(_drive.getDistance() <= -67628){
              if(!timerReset){
                timerReset = true;
                pathTimer.reset();
              }
                if(ballHandler.firstBall){
                red = true;
                System.out.println("RED");
                galaticMode = galaticSearch.DETERMINEPATH;
              }else if( pathTimer.get() > 1 && !ballHandler.firstBall){
                System.out.println("BLUE");
                blue = true;
              }
            }else{
              _drive.arcadeDrive(0.45, _drive.anglePID.calculate(_drive.getAngle()));
            }
        
      }else{
          if(_drive.getDistance() > -160503 && !ballHandler.firstBall){
            
            _drive.arcadeDrive(0.45, _drive.anglePID.calculate(_drive.getAngle()));
          }else{
            galaticMode = galaticSearch.DETERMINEPATH;
          }
        }
      

          break;
        case DETERMINEPATH:
        photonVision.updatePhoton();
          _drive.arcadeDrive(0, 0);
        if(red){
          if(photonVision.hasTarget){
            System.out.println("Red Path B");
            galaticMode = galaticSearch.REDPATH_B_BALL2;
          }else{
            System.out.println("Red Path A");
            galaticMode = galaticSearch.REDPATH_A_BALL2;
            _drive.anglePID.reset();
          }
        }else if (blue){
          if(photonVision.hasTarget){
            System.out.println("Blue Path B");
            galaticMode = galaticSearch.BLUEPATH_B_BALL2;
          }else{
            System.out.println("Blue Path A");
            galaticMode = galaticSearch.BLUEPATH_A_BALL2;
          }
         } break;
        case REDPATH_A_BALL2:
         if(driveForward){
          ballHandler.getball();
          if(driveToDistance(-63940)){
            galaticMode = galaticSearch.REDPATH_A_BALL3;
          }

         }else{
          if(_drive.driveAngle(30, 0, 2)){
            driveForward = true;
          }
        }


        System.out.println(_drive.getAngle());
          break;
        case REDPATH_A_BALL3:
        System.out.println("REDPATH_A_BALL3");
         break;
        case REDPATH_A_END:
          break;
        case BLUEPATH_A_BALL2:
        System.out.println("BLUE PATH A BALL 2");
          break;
        case BLUEPATH_A_BALL3:
          break;
        case BLUEPATH_A_END:
          break;
        case REDPATH_B_BALL2:
        System.out.println("RED PATH B BALL 2");
          break;
        case REDPATH_B_BALL3:
         break;
        case REDPATH_B_END:
          break;
        case BLUEPATH_B_BALL2:
        System.out.println("BLUE PATH B BALL 2");
          break;
        case BLUEPATH_B_BALL3:
          break;
        case BLUEPATH_B_END:
          break;
        
      }

    }

    @Override
    public void teleopInit() {
      ballHandler.setBrakeMode();
      _drive.teleopInit();
      spinner.teleopInit();
      shooter.setRPM(3100);
      limeLight.lightOn();
      
    }

    @Override
    public void teleopPeriodic() {
      photonVision.updatePhoton();
      if(ballHandler.readyforball()){
        //LEDS.setColor(0.77);
      }else if(driverStation.getMatchTime() <= 45.0d){
        LEDS.setColor(0.61);
      } else {
        LEDS.setColor(0.41);
      }
      
      _drive.teleopPeriodic();
      shooter.teleopPeriodic();
      ballHandler.telopPeriodic();
      liftLeveler.telopPeriodic();
      spinner.teleopPeriodic();
      limeLight.limePeriodic();
      
      
      SmartDashboard.putNumber("RPM", shooter.getCurrentRPM());
      

    }

    public void disabledInit(){
      ballHandler.setCoastMode();
      _drive.disabledInit();
      shooter.disabledInit();
      limeLight.lightOff();

    }

    public void testInit(){
      _drive.resetRightMotor();
       
    }

    private Timer shotTimer = new Timer();

    public void testPeriodic(){

     System.out.println(_drive.getDistance());

    }

}