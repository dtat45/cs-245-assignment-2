import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 
 * @author Dan Tat
 *
 */
public class ShoppingList {
	
	protected static List<String> storeList = new ArrayList<String>(); // List of all services
	protected static List<String> storeFiles = new ArrayList<String>(); // List of service files containing inventory
	protected static List<Double> totalCosts = new ArrayList<Double>(); // Holds the total cost of items, with respect to each store.
	protected static List<Boolean> hasAllItems = new ArrayList<Boolean>(); // Used to check that the store contains all of the inquired items.
	
	/**
	 * Creates the best shopping list (least cost) based on the items
	 * requested from the user. Returns the lowest cost of the best
	 * service to purchase from.
	 * @param storeDeliveryFees
	 * @throws IOException
	 */
	protected static void bestShoppingList() throws IOException {
		
		Scanner input = new Scanner(System.in);
		boolean finishedShopping = false;
		
		while(finishedShopping == false) {

			System.out.print("Enter your next item or 'done': ");
			String item = input.nextLine();
			if(compare("done",item)) {
				
				input.close();
				break;
			}
			System.out.print("Size: ");
			String size = input.nextLine();
			System.out.println("Quantity: ");
			int quantity = input.nextInt();
			
			if(searchForItem(item,size,quantity) == false)
				System.out.println("No such item/size exists.");
		}
		input.close();
		
		DecimalFormat df = new DecimalFormat("#.##");
		int cheapestStoreIndex = cheapestTotalCost();
		
		if(cheapestStoreIndex < 0)
			System.out.println("No services have all requested items in stock.");
		else
			System.out.println("Best price through " + storeList.get(cheapestStoreIndex) + 
					". " + "Your total cost: $" + df.format(totalCosts.get(cheapestStoreIndex)));
	} // bestShoppingList
	
	/** 
	 * Compares the total costs of all stores against each other,
	 * returning the index of the cheapest store. Returns -1 if 
	 * no store contains all requested items. 
	 */
	protected static int cheapestTotalCost() {
		
		// Holds the index of the store that contains and requested items and cheapest cost.
		int cheapestStoreIndex = 0;
		// Initializes cheapestStoreIndex to the first store that contains all the requested items.
		for(int i = 0; i<hasAllItems.size(); i++) {
			
			if(hasAllItems.get(i) == true)
				break;
			cheapestStoreIndex++;
		}
		if(cheapestStoreIndex == totalCosts.size())
			return -1;

		for(int i=cheapestStoreIndex; i<totalCosts.size(); i++) {
			
			if(totalCosts.get(i) < totalCosts.get(cheapestStoreIndex) && hasAllItems.get(i) == true)
				cheapestStoreIndex = i;
		}
		
		return cheapestStoreIndex;
	} // cheapestTotalCost

	/** 
	 * Adds the price of the found item to the total cost, 
	 * with respect to its store. 
	 */
	protected static void addToTotalCost(double cost,int indexOfStore,int quantity) {
		
		double newTotalCost = totalCosts.get(indexOfStore) + cost;
		totalCosts.set(indexOfStore,newTotalCost);
	} // addToTotalCost
	
	/** 
	 * Looks for the requested item across ALL services. If the item is found,
	 * the item cost is added to the total cost (for that specific service). 
	 * Returns true if the item exist in at least one services. Returns false 
	 * if the does not exist in any of the services.		
	 * @throws FileNotFoundException 
	 */
	protected static boolean searchForItem(String item, String size, int quantity) throws FileNotFoundException {
		
		List<Boolean> itemFound = new ArrayList<Boolean>();
		
		for(int i=0; i<storeFiles.size(); i++) {
			
			Scanner reader = new Scanner(new FileReader(storeFiles.get(i)));
			String itemBrand, itemName, itemSize, itemCost;
			
			while(reader.hasNextLine()) {
				
				String itemLine = reader.nextLine();
				Scanner lineReader = new Scanner(itemLine);
				lineReader.useDelimiter(", ");
				
				itemBrand = lineReader.next();
				itemName = lineReader.next();
				itemSize = lineReader.next();
				itemCost = lineReader.next();
				
				itemFound.add(i,false);
				// Checks each line to see if the item matches.
				if(compare(itemBrand,item) || compare(itemName,item)) {
					
					if(compare(itemSize,size)) {
						
						// If the item is matched, the price will be added to the total cost
						// and search in this specific service will cease.
						addToTotalCost(Double.valueOf(itemCost),i,quantity);
						itemFound.add(i,true);
						lineReader.close();
						break;
					} // if
				} // if
				lineReader.close();
			} // while
			reader.close();
		} // for
		
		// Sets allItemExists only if the item existed in at least one service
		if(itemFound.contains(true)) {
			
			for(int i=0; i<storeFiles.size(); i++) {
			
				if(itemFound.get(i) == false)
					hasAllItems.set(i,false);
			}
			return true;
		}
		else
			return false;
	} // searchForItem
	
	/**
	 * Compares the given item String to the itemString from the store.
	 * @param item
	 * @param storeItem
	 */
	protected static boolean compare(String item, String storeItem) {
		
		if(storeItem.toLowerCase().contains(item.toLowerCase()))
			return true;
		
		return false;
	} // compare
	
	/**
	 * Parses a config line, from the config file and initializes 
	 * all of the Lists.
	 * @param line
	 */
	protected static void parseConfigLine(String line) {
		
		Scanner reader = new Scanner(line);
		reader.useDelimiter(", ");
		String store,storeFile,deliveryPrice;
		
		reader.skip("service=");
		store = reader.next();
		storeFile = reader.next();
		storeFile = storeFile.replace(".csv",".txt");
		deliveryPrice = reader.next();
		
		// Initializes all Lists
		storeList.add(store);
		storeFiles.add(storeFile);
		totalCosts.add(Double.valueOf(deliveryPrice));
		hasAllItems.add(true);
		
		reader.close();
	}
	
	// Driver function
	public static void main(String[] args) throws IOException {
		
		Scanner reader = new Scanner(new FileReader(args[0]));
		// Scanner reader = new Scanner(new FileReader("config.txt"));
		
		while(reader.hasNextLine()) {
			
			parseConfigLine(reader.nextLine());
		}
		reader.close();
		
		bestShoppingList();
	}
}
