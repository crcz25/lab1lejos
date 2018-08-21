
public class Main extends Thread {

    public static void main(String [] args) {

    	LCD.drawString("Pattern Square", 0, 0);
		Button.waitForAnyPress();
		LCD.clear();
        UltrasonicSensor ultra = new UltrasonicSensor(SensorPort.S1);
        SoundSensor sound = new SoundSensor(SensorPort.S2);
        TouchSensor touch = new TouchSensor(SensorPort.S3);
        int i = 0;
        int soundValue = 0;
        int touchValue = 0;
        boolean clap = false;

        public void run() {
        	while(true) {
        		if()
        	}
        }

        while (!touch.isPressed() || clap == true || ultra.getDistance() < 15) {
        	 soundValue = sound.readValue();
        	 if (soundValue > 40){
        		 clap = true;
        	 }
			 Motor.A.forward();
			 Motor.C.forward();
			 Delay.msDelay(3000);
			 Motor.A.stop();
			 Motor.C.stop();
			 Motor.A.rotate(610);
			 //LCD.drawString("Turn #"+ i +" time",0,0);
			 //i++;
			}
        Motor.A.stop();
		Motor.C.stop();
		LCD.clear();
		LCD.drawString("Stop! ", 0, 0);

		Button.waitForAnyPress();
    }
}