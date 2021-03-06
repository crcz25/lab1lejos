import java.util.Arrays;

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
    Behavior locateSound = new LocateSound();
    Behavior colliding = new Colliding();
    Behavior beep = new Beep();
    Behavior escape = new Escape();
    Behavior[] behaviorList =
    {
    		normal,
    		locateSound,
    		colliding,
    		beep,
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
  		m_shape = SQUARE;
	}

	int STEP_SQUARE = 20;
	public void nextStepSquare(int s) {
		if (s < (STEP_SQUARE - 1)) {
			Motor.A.rotate(50, true);
			Motor.C.rotate(50, true);
			Delay.msDelay(75);	
		}  else {
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
  			Motor.A.setSpeed(1000);
			Motor.C.setSpeed(1000);
			
  			LCD.clear();
  			String shapeName = "square";
			LCD.drawString("Normal #" + m_step + " " + shapeName, 0, 0);

			if(SQUARE == m_shape) {
				nextStepSquare(m_step % STEP_SQUARE); 
			}
  			m_step++;
  		}
  	}
  }

  public static class Colliding implements Behavior
  {
  	private boolean m_suppressed = false;
  	private TouchSensor m_touchLeft;
  	//private TouchSensor m_touchRigth;

  	public Colliding() {
  		m_touchLeft = new TouchSensor(SensorPort.S4);
  		//m_touchRigth = new TouchSensor(SensorPort.S3);
  	}

  	public boolean takeControl()
  	{
  	    return  m_touchLeft.isPressed();
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
  
  public static class Beep implements Behavior {
  	private boolean m_suppressed = false;
  	private LightSensor ls = new LightSensor(SensorPort.S3);
  
  	public boolean takeControl()
  	{
  		if(ls.readValue() <= 30) {
			return true;
		}
  		return false;
  	}

  	public void suppress()
  	{
  		  m_suppressed = true;
  	}

  	public void action()
  	{
  		m_suppressed = false;
  		int lightValue = ls.readValue();
  		LCD.clear();
	    LCD.drawInt(lightValue, 4, 10, 2);
	    Sound.beepSequence();  		
	    try {
  			Thread.sleep(500);
  			Sound.pause(100);
  		} catch (InterruptedException e) {

  		}
  	}
  }
  
  
  public static class LocateSound implements Behavior {
		private SoundSensor m_sound = new SoundSensor(SensorPort.S2, true);
		private boolean m_suppressed = false;
	  	private LightSensor ls = new LightSensor(SensorPort.S3);

		
		private static int CONTROL_MEASUREMENTS = 5;

		private static int MIN_SOUND = 92;
		private static int MAX_ELAPSE = 20000;
		
		private long lastTime = 0;
		private int lastDirection = 0;
		int step = 0;
		
		public boolean takeControl() {
			return  m_sound.readValue() > MIN_SOUND;
		}
		
		public void rotateToSound() {
			Motor.A.setSpeed(320);
			Motor.C.setSpeed(320);
			
			int ROT_A = 1070 + 55;
			int ROT_C = -1100 + 55;
			int SLEEP = 42;
			
			
			Motor.A.rotate(ROT_A, true);
			Motor.C.rotate(ROT_C, true);
			
			SoundSensor m_sound = new SoundSensor(SensorPort.S2, true);
			
			int SIZE = 75;
			
			
			int[] m = new int[SIZE];
			
			for(int i = 0; i < 75; i++) {
				Delay.msDelay(SLEEP);
			
				m[i] = m_sound.readValue();
				m[i] += m_sound.readValue();
				m[i] += m_sound.readValue();
				m[i] += m_sound.readValue();
				m[i] += m_sound.readValue();
				
				if(ls.readValue() <= 30) {
					Motor.A.stop();
					Motor.C.stop();
					return;
				}
				
				System.out.println("M: " + m[i]);
				
			}
			Sound.beep();
			
			float maximo = 0;
			int maxIndex = 0;
			
			for(int i = 3; i < SIZE; i++) {
				float sum = 0;
				for(int j = 0; j < 7; j++) {
					sum += m[(i + j - 2 + SIZE) % (SIZE)];
				}
				
				System.out.println("R: " + sum);
				
				//Delay.msDelay(500);
				if(sum > maximo) {
					maximo = sum;
					maxIndex = i;
				}
			}
			
			
			System.out.println("D: " + maxIndex + " | " + maximo);
			
			float direction = ((float)maxIndex) / SIZE;
			
			direction *= 0.9;
			
			Motor.A.rotate((int)(ROT_A * direction), true);
			Motor.C.rotate((int)(ROT_C * direction), true);
			
			
			Delay.msDelay((int)(direction * 3200) + 300);
		}
		
		public void moveToSound() {
			Motor.A.setSpeed(1000);
			Motor.C.setSpeed(1000);
			
			Motor.A.rotate(50, true);
			Motor.C.rotate(50, true);
			Delay.msDelay(75);
		}
		

		public void action() {
			m_suppressed = false;
			long currentTime = System.currentTimeMillis();
			
			if((lastTime + MAX_ELAPSE) < currentTime) {
				step = 0;
			}
			
			LCD.drawString("LISTENING START", 0, 0);
			
			lastTime = currentTime;
			
			while (!m_suppressed )
	  		{
				LCD.drawString("LISTENING " + step, 0, 0);
				step++;
				if(step == 1) {
					this.rotateToSound();
				} else {
					this.moveToSound();
				}
				step %= 20;
				
				if(step == 0 && m_sound.readValue() < MIN_SOUND) {
					break;
				}
			} 
		}

		public void suppress() {
			m_suppressed = true;	
		}
		
	  }
  
  
  public static class Escape implements Behavior{
		public void action() {
			System.exit(0);
		}

		public void suppress() {

		}

		public boolean takeControl() {
			if(Button.ESCAPE.isPressed())
				return true;
			else return false;
		}
	}
}