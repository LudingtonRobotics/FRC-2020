package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.playingwithfusion.TimeOfFlight;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.Joystick;


 class BallHandler {
    
    //------MotorControllers------//
    private final CANSparkMax   _lowFeed = new CANSparkMax(RobotMap._lowFeed, MotorType.kBrushless);
    private final CANSparkMax   _upFeed  = new CANSparkMax(RobotMap._upFeed,  MotorType.kBrushless);
    private final WPI_VictorSPX _intake  = new WPI_VictorSPX(RobotMap._intake);
    //----------------------------//

    private Joystick MINIPJOY_1;
    private Joystick MINIPJOY_2;


    private enum autoMode{
        IDLE, INDEXUP
    }

    private autoMode mode;

    public BallHandler(Joystick MINIPJOY_1, Joystick MINIPJOY_2) {
        this.MINIPJOY_1 = MINIPJOY_1;
        this.MINIPJOY_2 = MINIPJOY_2;
        _lowFeed.setInverted(true);
        _upFeed.setInverted(true);
        mode = autoMode.IDLE;

    }

    public void autoInit(){
        mode = autoMode.IDLE;
    }


    public void telopPeriodic(){

        // INTAKE
        if(MINIPJOY_1.getRawButton(InputMap.INTAKE_IN)){
            _intake.set(0.25d);
        }else{
            _intake.set(0.0d);
        }

        // LOWER HOPPER
        if(MINIPJOY_1.getRawButton(InputMap.LOWER_HOPPER_UP)){
            _lowFeed.set(0.25d);
        }else{
            _lowFeed.set(0.0d);
        }

        // UPPER HOPPER
        if(MINIPJOY_1.getRawButton(InputMap.UPPER_HOPPER_UP)){
            _upFeed.set(0.25d);
        }else{
            _upFeed.set(0.0d);
        }

        if(!(MINIPJOY_1.getRawButton(InputMap.UPPER_HOPPER_UP) && MINIPJOY_1.getRawButton(InputMap.LOWER_HOPPER_UP))){
            if(MINIPJOY_1.getRawButton(InputMap.BACKFEED)){
                _upFeed.set(-0.25);
                _lowFeed.set(-0.25);
                _intake.set(-0.25d);
            }
        }
        //

    }
 
}