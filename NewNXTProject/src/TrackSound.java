import java.util.ArrayList;

import lejos.nxt.*;
import lejos.util.Delay;
import lejos.robotics.subsumption.*;

public class TrackSound
{

	public static int SQUARE = 0;
	public static int CIRCLE = 1;

	public static void main(String[] args)
	{
		int ROT_A = 1070 + 55;
		int ROT_C = -1100 + 55;
		int SLEEP = 42;
		
		Motor.A.setSpeed(320);
		Motor.C.setSpeed(320);
		
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
			
			System.out.println("M: " + m[i]);
			
		}
		Sound.beep();
		
		if(true) {
		
		
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
		
		
		Delay.msDelay(5000);
		}
		
		/*Behavior normal = new Normal();
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
			arbitrator.start();*/
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

  public static class LocateSound implements Behavior {
	private SoundSensor m_sound = new SoundSensor(SensorPort.S2);
	
	private static int CONTROL_MEASUREMENTS = 5;
	private static int ACTION_MEASUREMENTS = 7;

	private static int MIN_SOUND = 60;
	private static int MAX_ELAPSE = 20000;
	
	private long lastTime = 0;
	private int lastDirection = 0;
	
	private ArrayList<Measure> m_measurements;

	public boolean takeControl() {
		int[] measurements = new int[CONTROL_MEASUREMENTS];
		for(int i = 0; i < CONTROL_MEASUREMENTS; i++) {
			measurements[i] = m_sound.readValue();
		}
		
		return measurements[CONTROL_MEASUREMENTS / 2] > MIN_SOUND;
	}
	
	private float getMeasure() {
		int[] measurements = new int[ACTION_MEASUREMENTS];
		for(int i = 0; i < ACTION_MEASUREMENTS; i++) {
			measurements[i] = m_sound.readValue();
		}
		
		int middleIndex = ACTION_MEASUREMENTS / 2;
		float average = 0;
		for(int i = -1; i <= 1; i++) {
			average += measurements[middleIndex + i];
		}
		return average / 3;
	}

	public void action() {
		long currentTime = System.currentTimeMillis();
		
		if((lastTime + MAX_ELAPSE) < currentTime) {
			lastDirection = 0;
			m_measurements = new ArrayList<Measure>();
		}
		
		lastTime = currentTime;

		
		
		
	}

	public void suppress() {
		// TODO Auto-generated method stub
		
	}
	
	public class Measure {
		public float sound;
		public int direction;
		
		public Measure(float sound, int direction) {
			super();
			this.sound = sound;
			this.direction = direction;
		}
	}
	  
  }
}