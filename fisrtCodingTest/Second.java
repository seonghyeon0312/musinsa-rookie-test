import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Second {
	static int s, r;
	static Map<String, Store> stores = new HashMap<>();
	static int count = 0;
    public static void main(String[] args) throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	StringTokenizer st = new StringTokenizer(br.readLine(), ",");
    	
    	// S: 매장 수, R: 픽업 예약 수
    	s = Integer.parseInt(st.nextToken());
    	r = Integer.parseInt(st.nextToken());
    	
    	// 스토어 id, 오픈시간, 마감시간, 시간당 픽업 한도
    	for(int i=0;i<s;i++) {
    		st = new StringTokenizer(br.readLine(), ",");
    		String storeId = st.nextToken();
    		int open = Integer.parseInt(st.nextToken());
    		int close = Integer.parseInt(st.nextToken());
    		int hourPerLimit = Integer.parseInt(st.nextToken());
    		stores.put(storeId, new Store(storeId, open, close, hourPerLimit));
    	}
    	
    	// 스토어 id, 상품id, 수량
    	for(int i=0;i<s;i++) {
    		st = new StringTokenizer(br.readLine(), ",:");
    		String storeId = st.nextToken();
    		
    		while(st.hasMoreTokens()) {
    			String itemId = st.nextToken();
        		if(!itemId.equals("-")) {
            		String value = st.nextToken();
            		int stock = Integer.parseInt(value);
            		stores.get(storeId).addItem(itemId, stock);
        		}
    		}
    	}
        Queue<Order> queue = new LinkedList();
    	while(r-- > 0) {
    		// 요청 id, 스토어id, 상품id, 수량, 희망 시간
    		st = new StringTokenizer(br.readLine(), ",:");
    		String pickupId = st.nextToken();
    		String storeId = st.nextToken();
    		String itemId = st.nextToken();
    		int stock = Integer.parseInt(st.nextToken());
    		int pickupTimeHour = Integer.parseInt(st.nextToken()); // 시간단위
    		int pickupTimeMin = Integer.parseInt(st.nextToken()); // 분단위
    		
    		// pickup 얘약 생성
    		Order order = new Order(pickupId, storeId, itemId, stock, pickupTimeHour, pickupTimeMin);

    		queue.add(order);
    	}
    	int cnt = 0;
    	while(!queue.isEmpty()) {
    		Order order = queue.poll();
    		// pickup 예약 검증
    		order.validateOrder();
    		// pickup 예약 결과 출력
    		order.printResult();
    		if(order.result == 0) cnt++;
    	}
    	System.out.println(cnt);
    }
    
    static class Store{
    	String id;
    	int openTime;
    	int closeTime;
    	int[] limitPerHour = new int[25];
    	Map<String, Item> items = new HashMap<>();
    	
    	Store(String id, int open, int close, int limit){
    		this.id = id;
    		this.openTime = open;
    		this.closeTime = close;
    		
    		for(int i=0;i<25;i++) {
    			limitPerHour[i] = limit;
    		}
    	}
    	
    	void addItem(String id, int stock) {
    		Item item = new Item(id, stock);
    		items.put(id, item);
    	}
    	
    	boolean checkTime(int time) {
    		if(openTime<=time && time < closeTime) {
    			return true;
    		}
    		return false;
    	}
    	
    	boolean checkOrderLimit(int time) {
    		if(limitPerHour[time] == 0) return false;
    		return true;
    	}
    	
    	boolean checkStock(String itemId, int orderStock) {
    		Item item = items.get(itemId);
    		if(item == null) {
    			return false;
    		}
    		return item.checkStock(orderStock);
    	}
    	
    	void success(String itemId, int orderStock, int time) {
    		Item item = items.get(itemId);
    		item.decreaseStock(orderStock);
    		
    		limitPerHour[time]--;
    	}
    }
    
    static class Item{
    	String id;
    	// 실제로는 AtomicInteger로 하거나 동시성 제어 해야함
    	int stock;
    	
    	Item(String id, int stock){
    		this.id = id;
    		this.stock = stock;
    	}
    	
    	boolean checkStock(int orderStock) {
    		if(stock < orderStock) {
    			return false;
    		}
    		return true;
    	}
    	
    	void decreaseStock(int value) {
    		stock-=value;
    	}
    }
    static class Order{
    	String id;
    	String storeId;
    	String itemId;
    	int orderStock;
    	int pickupTimeHour;
    	int pickupTimeMin;
    	int result;
    	String[] resultReason = {"OK", "FAIL,STORE","FAIL,TIME","FAIL,FULL","FAIL,STOCK"};
    	
    	Order(String id, String store, String item, int stock, int hour, int minute){
    		this.id = id;
    		this.storeId = store;
    		this.itemId = item;
    		this.orderStock = stock;
    		this.pickupTimeHour = hour;
    		this.pickupTimeMin = minute;
    	}
    	
    	void validateOrder() {
    		// 조건 1 : 존재하는 매장
    		Store store = stores.get(storeId);
    		if(store == null) {
    			this.result = 1;
    			return;
    		}
    		// 조건 2 : 해당 매장의 오픈 시간 <= 픽업 시간 < 마감 시간
    		if(!store.checkTime(pickupTimeHour)) {
    			this.result = 2;
    			return;
    		}
    		
    		// 조건 3 : 희망 픽업 시간대의 픽업 개수가 매장의 한도 내여야 한다. -> 여기서 픽업 시간대란 11:30분이 픽업 시간이라면 오전 11시가 픽업 시간대가 된다.
    		if(!store.checkOrderLimit(pickupTimeHour)) {
    			this.result = 3;
    			return;
    		}
    		// 조건 4 : 픽업 요청의 요청 수량은 재고보다 이하여야 한다.
    		if(!store.checkStock(itemId, orderStock)) {
    			this.result = 4;
    			return;
    		}
    		// 조건 5 : 픽업 예약 성공 시 재고는 차감되고 해당 시간대의 매장 픽업 수는 +1
    		store.success(itemId,orderStock, pickupTimeHour);
    		result = 0;
    		count++;
    	}
    	
    	void printResult() {
    		StringBuilder sb = new StringBuilder();
    		String value = sb.append(this.id).append(",").append(resultReason[result]).toString();
    		System.out.println(value);
    	}
    }
}