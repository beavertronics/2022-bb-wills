// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.cameraserver.CameraServer;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  /*Auto Switching setup */
  private static final String kDefaultAuto = "Just move forwards and shoot and pray";
  //private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  //private final SendableChooser<String> m_chooser = new SendableChooser<>();

  /*
  Motor Drivers: 
  Marked as 2: CAN41
  Marked as 1: CAN42
  Marked as 4: CAN43
  Marked as 3: CAN44
  Marked as 6: CAN45
  Marked as 5: CAN46

  Current arrangement on board:

            6 3 1 (On Bottom)

            5 4 2 (On Top)
  (Far Left)     (Far right)

  Use markings on CONNECTORS, NOT ON MOTOR CONTROLLERS!

  
  Currently, 
  1 and 2 (42 & 41 on CAN) are the left-side drive motors,
  3 and 4 (44 & 3 on CAN) are the right-side drive motors,
  and 5   (46 on CAN) is Hold/Shoot.

  Why is it marked this way?

  I don't know!!!

  The Dual-Acting piston is controlled by Port 1 on the PCM, which is the one after port 0 (which is unused).

  */
  private final WPI_VictorSPX m_shootymotor = new WPI_VictorSPX(46);


  private final MotorController m_leftmotors = 
  new MotorControllerGroup(new WPI_VictorSPX(41), new WPI_VictorSPX(42));

  private final MotorController m_rightmotors = 
  new MotorControllerGroup(new WPI_VictorSPX(43), new WPI_VictorSPX(44));

  private final DifferentialDrive m_drive = new DifferentialDrive(m_leftmotors, m_rightmotors);

  private final Solenoid s_lift = new Solenoid(PneumaticsModuleType.CTREPCM, 1);

  private final Joystick joyL = new Joystick(0);
  private final Joystick joyR = new Joystick(1);

  private final XboxController joyXbox = new XboxController(3);
  private double autoStartTime = 0;

  private void bindButtons() {
    //TODO;
  }

  private final String[] things = {kDefaultAuto};

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    //m_chooser.setDefaultOption(kDefaultAuto + " (Default Auto)", kDefaultAuto);
    //m_chooser.addOption("Do Nothing???", "");
    SmartDashboard.putStringArray("Auto List", things);


    m_leftmotors.setInverted(true); //Making sure they go the right way
    m_rightmotors.setInverted(false);

    CameraServer.startAutomaticCapture();
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    //m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    autoStartTime = System.currentTimeMillis();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      /*case kCustomAuto:
        // Put custom auto code here
        break;*/
      case kDefaultAuto:
      default:
        if (System.currentTimeMillis() - autoStartTime > 750) {
          m_drive.stopMotor();
        } else {
          m_drive.tankDrive(-0.8, -0.8);
        }
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    System.out.println("Teleop Initialized!");
  }

  public double[] motorStats = {0,0,0,0}; 

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    double l = joyL.getY();
    double r = joyR.getY();

    if (l > 0.08) l = l/2 + 0.4; //Bump up motor power so stick operates in usable range
    if (r > 0.08) r = r/2 + 0.4; //Instead of having a large region where it's unresponsive
    if (l < -0.08) l = l/2 - 0.4;
    if (r < -0.08) r = r/2 -0.4;
    
    if (l > 1) l=1;
    if (r > 1) r=1;
    if (l < -1) l=-1;
    if (r < -1) r=-1;

    motorStats[0] = l;
    motorStats[1] = r;
    motorStats[2] = l;
    motorStats[3] = r;


    //Tank Drive
    m_drive.tankDrive(l, r);
    SmartDashboard.putNumberArray("RobotDrive Motors", motorStats);

    //Tube Lifting
    if (joyL.getTriggerPressed()) {
      s_lift.toggle();
    }

    //Storage/Shooter Control
    if (joyXbox.getAButtonPressed()) {
      //System.out.println("AAAAAAAAAAAAAAAAAAAAAAA"); screaming is just so unnecesary
      m_shootymotor.set(1);
    } else if (joyXbox.getStartButtonPressed()) {
      m_shootymotor.set(-1);
    } else if (joyXbox.getAButtonReleased() || joyXbox.getStartButtonReleased()) {
      m_shootymotor.set(0);
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    m_shootymotor.stopMotor();
    m_drive.stopMotor();
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
    System.out.println("Test Mode Was Removed!");
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
    
    //disabled
  }

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
