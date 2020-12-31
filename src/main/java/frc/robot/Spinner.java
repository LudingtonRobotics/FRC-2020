package frc.robot;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;

import edu.wpi.first.wpilibj.DriverStation;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.*;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;

class Spinner {

    private enum SpinnerModes   { SPINNER_IDLE, SPINNER_LIFT, SPINNER_SETUP, SPINNER_ROTATION, SPINNER_POSITION };
    private enum ColorState     { BLUE, RED, GREEN, YELLOW, UKNOWN};
    private final I2C.Port      i2cPort        = I2C.Port.kOnboard;
    private final WPI_VictorSPX  _colrWheel     = new WPI_VictorSPX(RobotMap._colrWhel);
    private Joystick    MINIPJOY_1;
    private Joystick MINIPJOY_2;
 
    private final ColorSensorV3 m_colorSensor  = new ColorSensorV3(i2cPort);
    private final ColorMatch    m_colorMatcher = new ColorMatch();
 
    //-------RGBColorValues-------//
    private final Color kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
    private final Color kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
    private final Color kRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
    private final Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);
    //----------------------------//

    private ColorState  validColor  = ColorState.UKNOWN; //Value of color to count
    private ColorState  tempColor    = ColorState.UKNOWN;
    private ColorState  fmsColor     = ColorState.UKNOWN;
    private int         colorCount      = 0;
    private int         gameStage       = 0;
    SpinnerModes        spinnerMode    = SpinnerModes.SPINNER_IDLE;

    private Led LEDS;

 



    /*
     *
     * This function is called periodically during test mode.
     */
    public Spinner( Led LEDS, Joystick MINIPJOY_1, Joystick MINIPJOY_2 ) {
        
        this.MINIPJOY_1 = MINIPJOY_1;
        this.MINIPJOY_2 = MINIPJOY_2;
        this.LEDS = LEDS;

        m_colorMatcher.addColorMatch(kBlueTarget);
        m_colorMatcher.addColorMatch(kGreenTarget);
        m_colorMatcher.addColorMatch(kRedTarget);
        m_colorMatcher.addColorMatch(kYellowTarget);

    }

    public void teleopInit(){
        spinnerMode = SpinnerModes.SPINNER_IDLE;
        _colrWheel.setNeutralMode(NeutralMode.Brake);
        
    }

    public void teleopPeriodic() {

        if(MINIPJOY_2.getRawButton(InputMap.COLOR_AUTO)){
            //TODO Change this to correct buttons when control board is done
            if(MINIPJOY_1.getRawButton(InputMap.COLOR_4_TIMES)){//Button to tell Spinner system to run rotation control
                gameStage = 2;
                spinnerMode = SpinnerModes.SPINNER_SETUP;
            }else if(MINIPJOY_1.getRawButton(InputMap.COLOR_POSITION)){//Button to tell Spinner system to run position control
                gameStage = 3;
                spinnerMode = SpinnerModes.SPINNER_SETUP;
            }

/*
*    Switch statement to control the different parts of the Spinner Control panel manipulator  
*/            
            switch (spinnerMode) {
                
/*
*   Sets the spinner components back to and idle state   
*/                
                case SPINNER_IDLE: //default position
                    _colrWheel.stopMotor();
                    colorCount = 0;
                    tempColor = ColorState.UKNOWN;                    
                break;
                 
/*
*   Rotates the arm into position   
*/                
                case SPINNER_LIFT: // move arm in to position
                    //TODO Rotate arm in to position
                    spinnerMode = SpinnerModes.SPINNER_SETUP;                   
                break;

/*
*   Decides which mode we are in   
*/
                case SPINNER_SETUP: //Checks if valid color and which part of game based on FMS data
                    colorMatching();
                    validColor = tempColor;
                    if(validColor != ColorState.UKNOWN){ //Checking to make sure the color is good then go to next step
                        if( gameStage == 2 ){
                            spinnerMode = SpinnerModes.SPINNER_ROTATION;
                        }else if ( gameStage == 3 ) {
                            spinnerMode = SpinnerModes.SPINNER_POSITION;  
                        }
                    }
                break;

/*
*   Rotation control   
*/
                case SPINNER_ROTATION: //Rotate 4 times
                    if (colorCount <= 31){ //rotate until color has changed 32 times = 4 full rotations
                        _colrWheel.set(.85); //This may be to fast??
                        
                        colorMatching();

                        if(validColor != tempColor && tempColor != ColorState.UKNOWN){ //If the color has actually changed count it and set new valid color
                            colorCount ++; //Add one for our color change
                            validColor = tempColor;  //Set the new color as Valid  
                        }
                    }else{
                        spinnerMode = SpinnerModes.SPINNER_IDLE;  
                    }

                    break;

/*
*   Position control
*/
                case SPINNER_POSITION:
                String gameData;
        
                gameData = DriverStation.getInstance().getGameSpecificMessage();
                    
                if(gameData.length() > 0){
//These have a 2 color offset to match the FMS sensor location
                    if ( gameData.charAt(0) == 'B') fmsColor = ColorState.RED;
                    if ( gameData.charAt(0) == 'R') fmsColor = ColorState.BLUE;
                    if ( gameData.charAt(0) == 'G') fmsColor = ColorState.YELLOW;
                    if ( gameData.charAt(0) == 'Y') fmsColor = ColorState.GREEN;

                }else{
                    
                    fmsColor = ColorState.UKNOWN; 
                }
                    
                    
                    colorMatching();

                    if ( fmsColor != tempColor && tempColor != ColorState.UKNOWN ){
                        _colrWheel.set(.5); 
                    }else{
                        spinnerMode = SpinnerModes.SPINNER_IDLE;  

                    }
                        break;
                }
            }else{
                //Color manual
                if(MINIPJOY_1.getRawButton(InputMap.COLOR_4_TIMES)){
                    _colrWheel.set(0.5d);
                }else if(MINIPJOY_1.getRawButton(InputMap.COLOR_POSITION)){
                    _colrWheel.set(-0.5d);
                }else
                    _colrWheel.set(0.0d);
            }
            SmartDashboard.putNumber("color count", colorCount );
            SmartDashboard.putString("First Color", validColor.toString() );
            SmartDashboard.putString("Current Color", tempColor.toString() );
            SmartDashboard.putString("CASE", spinnerMode.toString() );
            SmartDashboard.putString("FMS Color", fmsColor.toString() );

    }
 
/*
*   This is called to get the current color if it's a valid color
*/
     void colorMatching(){ 
        Color detectedColor = m_colorSensor.getColor();
       
        ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);
                
        if ( match.color == kBlueTarget && match.confidence > 0.93 ) {
            tempColor = ColorState.BLUE;
            System.out.println("Blue");
            LEDS.setColor(0.83);
        } else if ( match.color == kRedTarget  && match.confidence > 0.90 ) {
            tempColor = ColorState.RED;
            System.out.println("Red");
            LEDS.setColor(0.61);
        } else if ( match.color == kGreenTarget  && match.confidence > 0.94) {
            tempColor = ColorState.GREEN;
            System.out.println("Green");
            LEDS.setColor(0.77);
        } else if ( match.color == kYellowTarget && match.confidence > 0.93 ) {
            tempColor = ColorState.YELLOW;
            System.out.println("Yellow");
            LEDS.setColor(0.69);
        } else {
            tempColor = ColorState.UKNOWN;
            System.out.println("Unknown");
            LEDS.setColor(0.41);  
    }
        SmartDashboard.putNumber("Match Con", match.confidence);
    }

    
}