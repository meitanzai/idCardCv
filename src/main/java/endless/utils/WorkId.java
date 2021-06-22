package endless.utils;
/**
 * 这个有问题
 * @author Administrator
 *
 */
public class WorkId { 
	/**
	 *  下面的是copy mybaits-puls的id生成
	 */
	private static final Sequence worker = new Sequence();

	public static long getId() {
		return worker.nextId();
	}


	public static void main(String[] args) throws InterruptedException {
		for(int i=0;i<1000;i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println(getId());
					}catch (Exception e){

					}

				}
			}).start();
		}
		Thread.sleep(100000);
	}
}
