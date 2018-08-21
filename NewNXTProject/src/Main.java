import lejos.nxt.*;
import lejos.util.Delay;
import lejos.robotics.subsumption.*;

public class Main
{

	public static int SQUARE = 0;
	public static int CIRCLE = 1;

  public static void main(String[] args)
  {
	Motor.A.setSpeed(1000);
	Motor.C.setSpeed(1000);
    Behavior normal = new Normal();
    Behavior colliding = new Colliding();
    Behavior listening = new Listening((Normal)normal);
    Behavior escape = new Escape();
    Behavior[] behaviorList =
    {
    		normal,
    		colliding,
    		listening,
    		escape,
    };
    Arbitrator arbitrator = new Arbitrator(behaviorList);
		arbitrator.start();
  }

  public static class Normal implements Behavior
  {
  	private boolean m_suppressed = false;
  	private int m_step = 0;
  	private int m_shape = SQUARE;

  	public Normal() {
  		m_shape = SQUARE;
  	}

  	public boolean takeControl()
  	{
  	    return true;
  	}

  	public void suppress()
  	{
  		  m_suppressed = true;
  	}

  	public void toggleShape() {
  		m_shape = (SQUARE == m_shape)? CIRCLE : SQUARE;
		}

		int STEP_SQUARE = 20;
		public void nextStepSquare(int s) {
			if (s < (STEP_SQUARE - 1)) {
				Motor.A.rotate(50, true);
				Motor.C.rotate(50, true);
				Delay.msDelay(75);	
				
			} else {
				Motor.A.rotate(610);
				Motor.A.stop();
				Motor.C.stop();
			}
		}

		int STEP_CIRCLE = 4;
		public void nextStepCircle(int s) {

				Motor.A.forward();
				Delay.msDelay(200);
				Motor.A.stop();
			
		}

  	public void action()
  	{
			m_suppressed = false;
			
  		while (!m_suppressed)
  		{
  	  			LCD.clear();
  	  			String shapeName = (SQUARE == m_shape) ? "square" : "circle";
						LCD.drawString("Normal #" + m_step + " " + shapeName, 0, 0);

						if(SQUARE == m_shape) {
							nextStepSquare(m_step % STEP_SQUARE); 
						} else {
							nextStepCircle(m_step % STEP_CIRCLE);
						}
  	  			m_step++;
  		}
  	}
  }

  public static class Colliding implements Behavior
  {
  	private boolean m_suppressed = false;
  	private TouchSensor m_touchLeft;
  	private TouchSensor m_touchRigth;

  	public Colliding() {
  		m_touchLeft = new TouchSensor(SensorPort.S4);
  		m_touchRigth = new TouchSensor(SensorPort.S3);
  	}

  	public boolean takeControl()
  	{
  	    return m_touchRigth.isPressed() || m_touchLeft.isPressed();
  	}

  	public void suppress()
  	{
  		  m_suppressed = true;
  	}

  	public void action()
  	{
  		m_suppressed = false;
  		int i = 0;
  		LCD.clear();
  		LCD.drawString("Colliding #" + i, 0, 0);
  		Motor.A.backward();
		Motor.C.backward();
		Delay.msDelay(400);
		Motor.A.stop();
		Motor.C.stop();
  		i++;
  		try {
  			Thread.sleep(333);
  		} catch (InterruptedException e) {

  		}
  	}
  }

  public static class Listening implements Behavior {
  	private boolean m_suppressed = false;
  	private SoundSensor m_sound;
  	private Normal m_normal;

  	public Listening(Normal normal) {
  		m_sound = new SoundSensor(SensorPort.S2);
  		m_normal = normal;
  	}

  	public boolean takeControl()
  	{
  		int value = m_sound.readValue();
  		//System.out.println("TAKE CONTROL: " + value);
  	    return (value > 40);
  	}

  	public void suppress()
  	{
  		  m_suppressed = true;
  	}

  	public void action()
  	{
  		m_suppressed = false;
  		int soundValue = m_sound.readValue();
  		LCD.clear();
  		LCD.drawString("Clap #" + soundValue, 0, 0);
  		m_normal.toggleShape();
  		try {
  			Thread.sleep(2000);
  			Sound.pause(100);
  		} catch (InterruptedException e) {

  		}
  	}
  }
  
  public static class Escape implements Behavior{
		@Override
		public void action() {
			System.exit(0);
		}

		@Override
		public void suppress() {

		}

		@Override
		public boolean takeControl() {
			if(Button.ESCAPE.isPressed())
				return true;
			else return false;
		}
	}
}