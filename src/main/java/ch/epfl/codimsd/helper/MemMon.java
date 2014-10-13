package ch.epfl.codimsd.helper;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class MemMon {
	private volatile static String msg;
	private Timer timer;
	private long start;
	

	public MemMon() {
		msg = "";
		start = System.currentTimeMillis();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				long time = (System.currentTimeMillis() - start)/60000;
				System.out.println(msg + "Time(min): " + time + " - " + memoryInfo());
			}
		}, 0, 60000);
	}

	public static String getMsg() {
		return msg;
	}

	public static void setMsg(String msg) {
		MemMon.msg = msg;
	}

	public static String memoryInfo() {
		BigDecimal mega = new BigDecimal(1024 * 1024);
		BigDecimal total = new BigDecimal(Runtime.getRuntime().totalMemory()).divide(mega, 2, BigDecimal.ROUND_HALF_UP);
		BigDecimal free = new BigDecimal(Runtime.getRuntime().freeMemory()).divide(mega, 2, BigDecimal.ROUND_HALF_UP);
		BigDecimal max = new BigDecimal(Runtime.getRuntime().maxMemory()).divide(mega, 2, BigDecimal.ROUND_HALF_UP);
		return "Memory(MB): Total=" + total + ", Free=" + free + ", Max=" + max;
	}
	
	public static void main(String[] args) {
		MemMon mm = new MemMon();
		
		try {
			Class<?> c = Class.forName(args[0]);
			Method m = c.getMethod("main", String[].class);
			
			int size = args.length - 1;
			String[] args2 = new String[size]; 
			System.arraycopy(args, 1, args2, 0, size);

			//m.invoke(null, (Object[])args2);
			m.invoke(null, (Object)args2);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mm.timer.cancel();
	}

}
