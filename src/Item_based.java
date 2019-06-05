import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class Item_based {
	private double s_k_j[][];
	private HashMap<Integer, Set<Integer>> I_u;
	private HashMap<Integer, Set<Integer>> N_j;
	private HashMap<Integer, Set<Integer>> U_i;
	private double r_u_i[][];
	Record trainning[];
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Item_based IB = new Item_based();
		IB.readIn();
		IB.Initial();
		IB.CountSimilarityOfItems();
		IB.savePreditedRating();
	}
	Item_based() {
		s_k_j = new double[Data.ITEM_NUM + 1][Data.ITEM_NUM + 1];
		I_u = new HashMap<Integer, Set<Integer>>();
		N_j = new HashMap<Integer, Set<Integer>>();
		U_i = new HashMap<Integer, Set<Integer>>();
		trainning = new Record[Data.TRAINNING_RECORD_NUM];
		r_u_i = new double[Data.USER_NUM + 1][Data.ITEM_NUM + 1];
	}
	void Initial() {
	
		for (Record tmp : trainning) {
			if (!I_u.containsKey(tmp.userID)) {
				HashSet<Integer> tmpSet = new HashSet<Integer>();
				I_u.put(tmp.userID, tmpSet);
			}
			if (!U_i.containsKey(tmp.itemID)) {
				HashSet<Integer> tmpSet = new HashSet<Integer>();
				U_i.put(tmp.itemID, tmpSet);
			}
			I_u.get(tmp.userID).add(tmp.itemID);
			U_i.get(tmp.itemID).add(tmp.userID);
			
		
		}
		
		
		this.CountSimilarityOfItems();
	}
	void readIn() throws IOException {
		File baseFile = new File(Data.BASE_PATH) ;
		FileInputStream baseFileIn = new FileInputStream(baseFile);
		InputStreamReader baseIn = new InputStreamReader(baseFileIn);
		BufferedReader baseReader =  new BufferedReader(baseIn);
		for (int i = 0; i < Data.TRAINNING_RECORD_NUM; i++) {
			String data[] = baseReader.readLine().split("\\s+");
			Record newRecord = new Record(Integer.valueOf(data[0]), Integer.valueOf(data[1]), Double.valueOf(data[2]));
			trainning[i] = newRecord;
		}
		baseReader.close();
		baseIn.close();
		baseFileIn.close();
		
	
		
	}
	void CountSimilarityOfItems() {
		for (int k = 1; k <= Data.ITEM_NUM; k++) {
			
			for (int j = 1; j <= Data.ITEM_NUM; j++) {
				if (k == j) continue;
				if (!U_i.containsKey(k) || !U_i.containsKey(j)) {  
					continue;
				}
				Set<Integer> setkj = new HashSet<Integer>(U_i.get(k));
				setkj.retainAll(U_i.get(j));
				if (setkj.size() == 0) {
					continue;
				}
				s_k_j[k][j] = setkj.size() / U_i.get(k).size();
			//	if (s_k_j[k][j] < 0.195) s_k_j[k][j] = 0;
				if (s_k_j[k][j] == 0) continue;
				if (!N_j.containsKey(k)) {
					Set<Integer> tmp = new HashSet<Integer>();
					tmp.add(j);
					N_j.put(k, tmp);
				}
				else 
					N_j.get(k).add(j);
				
				if (!N_j.containsKey(j)) {
					Set<Integer> tmp = new HashSet<Integer>();
					tmp.add(k);
					N_j.put(j, tmp);
				}
				else N_j.get(j).add(k); 
				
			}
		}
	}
	void savePreditedRating() throws IOException {
		File f = new File(Data.PRE_RESULT_PATH);
		if (!f.exists()) f.createNewFile();		
		FileWriter fw = new FileWriter(f);
		BufferedWriter save = new BufferedWriter(fw);		
		String data;
		for (int u = 1; u <= Data.USER_NUM; u++) {
			for (int i = 1; i <= Data.ITEM_NUM; i++) {
				if (r_u_i[u][i] == 0) {
					double result = 0;
					
					if (!I_u.containsKey(u) || !N_j.containsKey(i) || N_j.get(i).size() == 0) { 
						result = 0;
					}
					else {
						final int jj = i;
						TreeSet<Integer> N_u_j= new TreeSet<Integer>(new Comparator<Integer>() {
							public int compare(Integer k1, Integer k2) {
								return s_k_j[jj][k1] - s_k_j[jj][k2] < 0 ? 1 : -1;
							}
						});
						N_u_j.addAll(N_j.get(i));
						N_u_j.retainAll(I_u.get(u));
						if (N_u_j.size() == 0) {
							result = 0;
						}
						else {
							double sum = 0;
							N_u_j.comparator();
							int t = Math.min(Data.K, N_u_j.size());
							for (Iterator<Integer> iter = N_u_j.iterator(); iter.hasNext(); ) {
								int k = iter.next();
								sum += s_k_j[k][i];
								t--;
								if (t == 0) break;
							}			
							result = sum;								
						}	
					}
				
					if (result >= 4) {
						data = u + " " + i + " " + result + '\n';
						save.write(data);
					}
						
				}
			
			}
		
		}
		save.flush();
		save.close();
	}
}
