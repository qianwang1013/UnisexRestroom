import java.util.Random;

class UnisexRestroom{
 	private State currentState;
 	private Human[] human; 
 	private int menIn;
 	private int womenIn;
	private Object lock = new Object();
    static private Random random = new Random();

 	public static void main(String[] arg){
 		new UnisexRestroom(10, 10);
 	}

 	public UnisexRestroom(int man, int woman){
 		initialize(man, woman);
 		startSimulation();
 		sleep(60000);
 		shutdownSimulation();
 	}

	private void sleep( int duration ){
	    try
	    {
	      Thread.currentThread().sleep( duration );
	    }
	    catch ( InterruptedException e )
	    {
	      Thread.currentThread().interrupt();
	    }
	}
 	private void shutdownSimulation(){
 		for(int i = 0; i != human.length; ++i){
 				human[i].interrupt();
 		}		
 	}
 	private void initialize(int man, int woman){
 		//initialize a array of users who is going to use the bathroom;
 		this.human = new Human[man + woman];
 		for(int i = 0; i != man + woman; ++i){
 			for(int k = 0; k != woman; ++k){
 				this.human[k] = new Woman(k);
 			}
 			for(int j = woman; j != man + woman; ++j){
 				this.human[j] = new Man(j);
 			}
 		}
 		this.currentState = new Empty();
 	}

 	private void startSimulation(){
 		int n = human.length; 

 		System.out.println("There are " + n + " human planing using the bathrom and " + menIn + " men in the bathroom");
		for(int i = 0; i != n; ++i){
 			human[i].start();
 		}
 	}

 	public State getState(){
 		return currentState;
 	}

	class Woman extends Human{	
		private String name;
		private int inBathroom = 0;
		private int outBathroom = 0;

		public Woman(int i){
			this.name = "Woman " + i;
		}
		public void wantsEnter() throws InterruptedException{
			synchronized(lock){
				System.out.println(name + " wants to enter");
				while(!currentState.wantsEnter()){
					lock.wait();
				}

				womenIn ++;
				System.out.println(name + " is in");
			}
		}
		public void leave(){
			synchronized(lock){
				currentState.leave();
				System.out.println(name + " left");

				lock.notifyAll();
			}
		}


		public void run(){
			try{
				while(!interrupted()){
					wantsEnter();
					inBathroom += doAction("do bussiness");
					leave();	
					//I need time to charge up my bladder
					outBathroom += doAction("recharge liquid");				
				}
	
			}
			catch(InterruptedException e){
				System.out.println(printResult());
			}
		}
		private int doAction( String act ) throws InterruptedException{
		    int time = random.nextInt( 4000 ) + 1000 ;
		    System.out.println( name + " is begining to " + act + " for " + time + 
					" milliseconds" );
		    sleep( time );

		    System.out.println( name + " is done " + act + "ing" );

		    return time;
		}
		public String toString(){
			return "Woman";
		}
		public String printResult(){
			return name + "Spend " + inBathroom + " inside Bathroom and " + outBathroom + " outBathroom recharging herself with liquid";
		}
	}

	class Man extends Human{
		private String name;
		private int inBathroom = 0;
		private int outBathroom = 0;

		public Man(int i){
			this.name = "Man " + i;
		}
		public void wantsEnter() throws InterruptedException{
			synchronized(lock){
				System.out.println(name + " wants to enter");
				while(!currentState.wantsEnter()){
					lock.wait();
				}

				menIn ++;
				System.out.println(name + " is in");
			}
			
		}
		private int doAction( String act ) throws InterruptedException{
		    int time = random.nextInt( 4000 ) + 1000 ;
		    System.out.println( name + " is begining to " + act + " for " + time + 
					" milliseconds" );
		    sleep( time );

		    System.out.println( name + " is done " + act + "ing" );

		    return time;
		}

		public void leave(){
			synchronized(lock){
				currentState.leave();
				System.out.println(name + " left");

				lock.notifyAll();
			}
		}

		public void run(){
			try{
				while(!interrupted()){
					wantsEnter();
					inBathroom += doAction("do bussiness");
					leave();					
					outBathroom += doAction("recharge liquid");
				}
				
			}
			catch(InterruptedException e){
				System.out.println(printResult());
			}
		}

		public String toString(){
			return "Man";
		}
		public String printResult(){
			return name + "Spend " + inBathroom + " inside Bathroom and " + outBathroom + " outBathroom recharging himself with liquid";
		}
	}


	class Empty extends State{
		public boolean wantsEnter(){
			synchronized(lock){
				if(((Human)Thread.currentThread()).toString() == "Woman"){
					currentState = new WomenOnly();
					return true;

				}
				else if(((Human)Thread.currentThread()).toString() == "Man"){
					currentState = new ManOnly();
					return true;
				}
				else{
					throw new RuntimeException("Some other gender tries to enter");
				}				
			}

		}
		public String toString(){
			return "Empty";
		}		
		public void leave(){
			synchronized(lock){
				System.out.println("There is no one here!");
				lock.notifyAll();
			}

		}

	}
	class ManOnly extends State{
		public boolean wantsEnter(){
			synchronized(lock){
				if(womenIn == 0 && menIn == 0){
					currentState = new Empty();
					return true;
				}
				else if(((Human)Thread.currentThread()).toString() == "Man"){
					return true;
				}
				else if(((Human)Thread.currentThread()).toString() == "Woman"){
					currentState = new WomenWantIn();
					return false;
				}
				else{
					throw new RuntimeException("Some other gender tries to enter");
				}				
			}

		}
		public String toString(){
			return "ManOnly";
		}
		public void leave(){
			synchronized(lock){

				if(((Human)Thread.currentThread()).toString() == "Man" && menIn != 0){
					--menIn;
				}
				else{
					throw new RuntimeException("why are you here?");
				}				
			}
				
		}		
	}
	class WomenWantIn extends State{
		public boolean wantsEnter(){
			synchronized(lock){
				if(((Human)Thread.currentThread()).toString() == "Man"){
					System.out.println("Be a gentleman, let the lady go first");
					return false;
				}
				else if(((Human)Thread.currentThread()).toString() == "Woman"){
					if(womenIn == 0 && menIn == 0){
						currentState = new WomenOnly();
						return true;
					}
					else{
						return false;
					}
				}
				else{
					throw new RuntimeException("Some other gender tries to enter");
				}					
			}
	
		}	
		public String toString(){
			return "WomenWantIn";
		}

		public void leave(){
			synchronized(lock){
				System.out.println((Human)Thread.currentThread() + " is trying to leave");
				if(womenIn == 0 && menIn == 0){
					currentState = new WomenOnly();
				}
				else if(((Human)Thread.currentThread()).toString() == "Man" && menIn != 0){
					--menIn;
				}
				else{
					throw new RuntimeException("why are you here?");
				}				
			}
			
		}	
	}
	class WomenOnly extends State{
		public boolean wantsEnter(){
			synchronized(lock){
				if(womenIn == 0 && menIn == 0){
					currentState = new Empty();
					return true;
				}
				else if(((Human)Thread.currentThread()).toString() == "Woman"){
					return true;

				}
				else if(((Human)Thread.currentThread()).toString() == "Man"){
					return false;
				}
				else{
					throw new RuntimeException("Some other gender tries to enter");
				}				
			}

		}

		public String toString(){
			return "WomenOnly";
		}

		public void leave(){
			synchronized(lock){
				System.out.println((Human)Thread.currentThread() + " is trying to leave");
				if(womenIn == 0 && menIn == 0){
					currentState = new Empty();
				}
				else if(((Human)Thread.currentThread()).toString() == "Woman" && womenIn != 0){
					--womenIn;
				}
				else{
					throw new RuntimeException("why are you here?");
				}					
			}
		}
	}


//	define types
	abstract class Human extends Thread{
		abstract protected void wantsEnter()  throws InterruptedException;
		abstract protected void leave() throws InterruptedException;
		public String toString(){
			return "";
		};
		abstract protected String printResult();
	}
	abstract class State{
		abstract public boolean wantsEnter();
		abstract public void leave();		
		public String toString(){
			return "";
		}
	}
}
