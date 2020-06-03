package exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/**
 * 
 * @Description:[�Զ�ȥֱ����ݹ顢�Զ�����FIRST��FOLLOW���ϡ��Զ�����Ԥ����������뵥�ʴ������Ƶ�����]
 * @author kailang
 * @date 2020-4-25
 * 
 */
public class Parser {
	public static final String PATH = "./grammar8";// �ķ�
	private static String START; // ��ʼ����
	private static HashSet<String> VN, VT; // ���ս���ż����ս���ż�
	private static HashMap<String, ArrayList<ArrayList<String>>> MAP;// key:����ʽ��� value:����ʽ�ұ�(������)
	private static HashMap<String, String> oneLeftFirst;// "|" �ֿ��ĵ�������ʽ��Ӧ��FIRST����,���ڹ���Ԥ�������
	private static HashMap<String, HashSet<String>> FIRST, FOLLOW; // FIRST��FOLLOW����
	private static String[][] FORM; // ���Ԥ�����������飬�������
	private static HashMap<String, String> preMap;// ���Ԥ��������map�����ڿ��ٲ���

	public static void main(String[] args) {
		init(); // ��ʼ������
		identifyVnVt(readFile(new File(PATH)));// ���ŷ���,����key-value��ʽ����MAP��
		reformMap();// ������ݹ����ȡ������
		findFirst(); // ��FIRST����
		findFollow(); // ��FOLLOW����
		// �ϵ����
//		VN.toString();
//		VT.toString();
//		MAP.toString();
//		FOLLOW.toString();
//		FIRST.toString();
//		oneLeftFirst.toString();
		if (isLL1()) {
			preForm(); // ����Ԥ�������
			// printAutoPre("aacbd"); // ʾ���Ƶ�
			System.out.println("������Ҫ�����ĵ��ʴ�:");
			Scanner in = new Scanner(System.in);
			printAutoPre(in.nextLine());
			in.close();
		}
	}

	// ������ʼ��
	private static void init() {
		VN = new HashSet<>();
		VT = new HashSet<>();
		MAP = new HashMap<>();
		FIRST = new HashMap<>();
		FOLLOW = new HashMap<>();
		oneLeftFirst = new HashMap<>();
		preMap = new HashMap<>();
	}

	// �ж��Ƿ���LL(1)�ķ�
	private static boolean isLL1() {
		System.out.println("\n�����ж��Ƿ���LL(1)�ķ�....");
		boolean flag = true;// ����Ƿ���LL(1)�ķ�
		Iterator<String> it = VN.iterator();
		while (it.hasNext()) {
			String key = it.next();
			ArrayList<ArrayList<String>> list = MAP.get(key);// ��������ʽ
			if (list.size() > 1) // �����������ʽ����߰�������ʽ�����ϣ�������ж�
				for (int i = 0; i < list.size(); i++) {
					String aLeft = String.join("", list.get(i).toArray(new String[list.get(i).size()]));
					for (int j = i + 1; j < list.size(); j++) {
						String bLeft = String.join("", list.get(j).toArray(new String[list.get(j).size()]));
						if (aLeft.equals("��") || bLeft.equals("��")) { // (1)��b����,��ҪFIRST(A)��FOLLOW(A)=��
							HashSet<String> retainSet = new HashSet<>();
							// retainSet=FIRST.get(key);//��ҪҪ����������޸�retainSetʱFIRSTͬ���ᱻ�޸�
							retainSet.addAll(FIRST.get(key));
							if (FOLLOW.get(key) != null)
								retainSet.retainAll(FOLLOW.get(key));
							if (!retainSet.isEmpty()) {
								flag = false;// ����LL(1)�ķ������FIRST(a)FOLLOW(a)�Ľ���
								System.out.println("\tFIRST(" + key + ") �� FOLLOW(" + key + ") = {"
										+ String.join("��", retainSet.toArray(new String[retainSet.size()])) + "}");
								break;
							} else {
								System.out.println("\tFIRST(" + key + ") �� FOLLOW(" + key + ") = ��");
							}
						} else { // (2)b!������,��ҪFIRST(a)��FIRST(b)= ��
							HashSet<String> retainSet = new HashSet<>();
							retainSet.addAll(FIRST.get(key + "��" + aLeft));
							retainSet.retainAll(FIRST.get(key + "��" + bLeft));
							if (!retainSet.isEmpty()) {
								flag = false;// ����LL(1)�ķ������FIRST(a)FIRST(b)�Ľ���
								System.out.println("\tFIRST(" + aLeft + ") �� FIRST(" + bLeft + ") = {"
										+ String.join("��", retainSet.toArray(new String[retainSet.size()])) + "}");
								break;
							} else {
								System.out.println("\tFIRST(" + aLeft + ") �� FIRST(" + bLeft + ") = ��");
							}
						}
					}
				}
		}
		if (flag)
			System.out.println("\t��LL(1)�ķ�,��������!");
		else
			System.out.println("\t����LL(1)�ķ�,�˳�����!");
		return flag;
	}
	
	
	// ����Ԥ�������FORM
	private static void preForm() {
		HashSet<String> set = new HashSet<>();
		set.addAll(VT);
		set.remove("��");
		FORM = new String[VN.size() + 1][set.size() + 2];
		Iterator<String> itVn = VN.iterator();
		Iterator<String> itVt = set.iterator();

		// (1)��ʼ��FORM,������oneLeftFirst(VN$VT,����ʽ)���
		for (int i = 0; i < FORM.length; i++)
			for (int j = 0; j < FORM[0].length; j++) {
				if (i == 0 && j > 0) {// ��һ��ΪVt
					if (itVt.hasNext()) {
						FORM[i][j] = itVt.next();
					}
					if (j == FORM[0].length - 1)// ���һ�м���#
						FORM[i][j] = "#";
				}
				if (j == 0 && i > 0) {// ��һ��ΪVn
					if (itVn.hasNext())
						FORM[i][j] = itVn.next();
				}
				if (i > 0 && j > 0) {// ��������ȸ���oneLeftFirst���
					String oneLeftKey = FORM[i][0] + "$" + FORM[0][j];// ��Ϊkey������First����
					FORM[i][j] = oneLeftFirst.get(oneLeftKey);
				}
			}
		// (2)������Ƴ��˦ţ������FOLLOW���
		for (int i = 1; i < FORM.length; i++) {
			String oneLeftKey = FORM[i][0] + "$��";
			if (oneLeftFirst.containsKey(oneLeftKey)) {
				HashSet<String> followCell = FOLLOW.get(FORM[i][0]);
				Iterator<String> it = followCell.iterator();
				while (it.hasNext()) {
					String vt = it.next();
					for (int j = 1; j < FORM.length; j++)
						for (int k = 1; k < FORM[0].length; k++) {
							if (FORM[j][0].equals(FORM[i][0]) && FORM[0][k].equals(vt))
								FORM[j][k] = oneLeftFirst.get(oneLeftKey);
						}
				}
			}
		}

		// ��ӡԤ���,������Map�����ݽṹ�����ڿ��ٲ���
		System.out.println("\n���ķ���Ԥ�������Ϊ��");
		for (int i = 0; i < FORM.length; i++) {
			for (int j = 0; j < FORM[0].length; j++) {
				if (FORM[i][j] == null)
					System.out.print(" " + "\t");
				else {
					System.out.print(FORM[i][j] + "\t");
					if (i > 0 && j > 0) {
						String[] tmp = FORM[i][j].split("��");
						preMap.put(FORM[i][0] + "" + FORM[0][j], tmp[1]);
					}
				}
			}
			System.out.println();
		}
		System.out.println();
	}


	// ����ĵ��ʴ������Ƶ�����
	public static void printAutoPre(String str) {
		System.out.println(str + "�ķ�������:");
		Queue<String> queue = new LinkedList<>();// ���Ӳ�ִ��ڶ���
		for (int i = 0; i < str.length(); i++) {
			String t = str.charAt(i) + "";
			if (i + 1 < str.length() && (str.charAt(i + 1) == '\'' || str.charAt(i + 1) == '��')) {
				t += str.charAt(i + 1);
				i++;
			}
			queue.offer(t);
		}
		queue.offer("#");// "#"����
		// ����ջ
		Stack<String> stack = new Stack<>();
		stack.push("#");// "#"��ʼ
		stack.push(START);// ��̬Ϊ��ʼ����
		boolean isSuccess = false;
		int step = 1;
		while (!stack.isEmpty()) {
			String left = stack.peek();
			String right = queue.peek();
			//System.out.println("left:"+left+" right:"+right);
			// (1)�����ɹ�
			if (left.equals(right) && right.equals("#")) {
				isSuccess = true;
				System.out.println((step++) + "\t#\t#\t" + "�����ɹ�");
				break;
			}
			// (2)ƥ��ջ���͵�ǰ���ţ���Ϊ�ս���ţ���ȥ
			if (left.equals(right)) {
				String stackStr = String.join("", stack.toArray(new String[stack.size()]));
				String queueStr = String.join("", queue.toArray(new String[queue.size()]));
				System.out.println((step++) + "\t" + stackStr + "\t" + queueStr + "\tƥ��ɹ�" + left);
				stack.pop();
				queue.poll();
				continue;
			}
			// (3)��Ԥ����в�ѯ
			if (preMap.containsKey(left + right)) {
				String stackStr = String.join("", stack.toArray(new String[stack.size()]));
				String queueStr = String.join("", queue.toArray(new String[queue.size()]));
				System.out.println((step++) + "\t" + stackStr + "\t" + queueStr + "\t��" + left + "��"
						+ preMap.get(left + right) + "," + right + "�����ջ");
				stack.pop();
				String tmp = preMap.get(left + right);
				//System.out.println("tmp: "+tmp);
				for (int i = tmp.length() - 1; i >= 0; i--) {// �����ջ
					String t = "";
					if (tmp.charAt(i) == '\'' || tmp.charAt(i) == '��') {
						t = tmp.charAt(i-1)+""+tmp.charAt(i);
						i--;
					}else {
						t=tmp.charAt(i)+"";
					}
					if (!t.equals("��"))
						stack.push(t);
				}
				continue;
			}
			break;// (4)�������ʧ�ܲ��˳�
		}
		if (!isSuccess)
			System.out.println((step++) + "\t#\t#\t" + "����ʧ��");
	}


	// ���ŷ���
	private static void identifyVnVt(ArrayList<String> list) {
		START = list.get(0).charAt(0) + "";// ��ſ�ʼ����

		for (int i = 0; i < list.size(); i++) {
			String oneline = list.get(i);
			String[] vnvt = oneline.split("��");// �ö�����ŷָ�
			String left = vnvt[0].trim(); // �ķ������
			VN.add(left);

			// �ķ��ұ�
			ArrayList<ArrayList<String>> mapValue = new ArrayList<>();
			ArrayList<String> right = new ArrayList<>();

			for (int j = 0; j < vnvt[1].length(); j++) { // �� ��|���ָ��ұ�
				if (vnvt[1].charAt(j) == '|') {
					VT.addAll(right);
					mapValue.add(right);
					// right.clear();// ���֮����Ȼ��ͬһ����ַ����Ҫ����new����
					right = null;
					right = new ArrayList<>();
					continue;
				}
				// �������ʽĳ�ַ�����ߺ������Ļ�Ӣ�ĵĵ����ţ�����Ϊͬһ���ַ�
				if (j + 1 < vnvt[1].length() && (vnvt[1].charAt(j + 1) == '\'' || vnvt[1].charAt(j + 1) == '��')) {
					right.add(vnvt[1].charAt(j) + "" + vnvt[1].charAt(j + 1));
					j++;
				} else {
					right.add(vnvt[1].charAt(j) + "");
				}
			}
			VT.addAll(right);
			mapValue.add(right);

			MAP.put(left, mapValue);
		}
		VT.removeAll(VN); // ���ս��ַ������Ƴ����ս��
		// ��ӡVn��Vt
		System.out.println("\nVn����:\t{" + String.join("��", VN.toArray(new String[VN.size()])) + "}");
		System.out.println("Vt����:\t{" + String.join("��", VT.toArray(new String[VT.size()])) + "}");

	}

	// ��ÿ�����ս���ŵ�FIRST���� �� �ֽⵥ������ʽ��FIRST����
	private static void findFirst() {
		System.out.println("\nFIRST����:");
		Iterator<String> it = VN.iterator();
		while (it.hasNext()) {
			HashSet<String> firstCell = new HashSet<>();// ��ŵ������ս���ŵ�FIRST
			String key = it.next();
			ArrayList<ArrayList<String>> list = MAP.get(key);
			// System.out.println(key+":");
			// ������������ʽ�����
			for (int i = 0; i < list.size(); i++) {
				ArrayList<String> listCell = list.get(i);// listCellΪ��|���ָ����
				HashSet<String> firstCellOne = new HashSet<>();// ����ʽ����á� | ���ָ�ĵ���ʽ�ӵ�First(����)
				String oneLeft = String.join("", listCell.toArray(new String[listCell.size()]));
				// System.out.println("oneLeft: "+oneLeft);
				if (VT.contains(listCell.get(0))) {
					firstCell.add(listCell.get(0));
					firstCellOne.add(listCell.get(0));
					oneLeftFirst.put(key + "$" + listCell.get(0), key + "��" + oneLeft);
				} else {
					boolean[] isVn = new boolean[listCell.size()];// ����Ƿ��ж���Ϊ��,�����������һ���ַ�
					isVn[0] = true;// ��һ��Ϊ���ս����
					int p = 0;
					while (isVn[p]) {
						// System.out.println(p+" "+listCell.size());
						if (VT.contains(listCell.get(p))) {
							firstCell.add(listCell.get(p));
							firstCellOne.add(listCell.get(p));
							oneLeftFirst.put(key + "$" + listCell.get(p), key + "��" + oneLeft);
							break;
						}
						String vnGo = listCell.get(p);//
						Stack<String> stack = new Stack<>();
						stack.push(vnGo);
						while (!stack.isEmpty()) {
							ArrayList<ArrayList<String>> listGo = MAP.get(stack.pop());
							for (int k = 0; k < listGo.size(); k++) {
								ArrayList<String> listGoCell = listGo.get(k);
								if (VT.contains(listGoCell.get(0))) { // �����һ���ַ����ս����
									if (listGoCell.get(0).equals("��")) {
										if (!key.equals(START)) { // ��ʼ���Ų����Ƴ���
											firstCell.add(listGoCell.get(0));
											firstCellOne.add(listGoCell.get(0));
											oneLeftFirst.put(key + "$" + listGoCell.get(0), key + "��" + oneLeft);
										}
										if (p + 1 < isVn.length) {// ���Ϊ�գ����Բ�ѯ��һ���ַ�
											isVn[p + 1] = true;
										}
									} else { // �ǿյ��ս���ż����Ӧ��FIRST����
										firstCell.add(listGoCell.get(0));
										firstCellOne.add(listGoCell.get(0));
										oneLeftFirst.put(key + "$" + listGoCell.get(0), key + "��" + oneLeft);
									}
								} else {// �����ս���ţ���ջ
									stack.push(listGoCell.get(0));
								}
							}
						}
						p++;
						if (p > isVn.length - 1)
							break;
					}
				}
				FIRST.put(key + "��" + oneLeft, firstCellOne);
			}
			FIRST.put(key, firstCell);
			// ���key��FIRST����
			System.out.println(
					"\tFIRST(" + key + ")={" + String.join("��", firstCell.toArray(new String[firstCell.size()])) + "}");
		}
	}

	// ��ÿ�����ս���ŵ�FLLOW����
	private static void findFollow() {
		System.out.println("\nFOLLOW����:");
		Iterator<String> it = VN.iterator();
		HashMap<String, HashSet<String>> keyFollow = new HashMap<>();

		ArrayList<HashMap<String, String>> vn_VnList = new ArrayList<>();// ���ڴ��/A->...B ���� A->...B�ŵ����

		HashSet<String> vn_VnListLeft = new HashSet<>();// ���vn_VnList����ߺ��ұ�
		HashSet<String> vn_VnListRight = new HashSet<>();
		// ��ʼ���ż���#
		keyFollow.put(START, new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				add(new String("#"));
			}
		});

		while (it.hasNext()) {
			String key = it.next();
			ArrayList<ArrayList<String>> list = MAP.get(key);
			ArrayList<String> listCell;

			// �Ȱ�ÿ��VN��ΪkeyFollow��key��֮���ڲ��������FOLLOWԪ��
			if (!keyFollow.containsKey(key)) {
				keyFollow.put(key, new HashSet<>());
			}
			keyFollow.toString();

			for (int i = 0; i < list.size(); i++) {
				listCell = list.get(i);

				// (1)ֱ���ҷ��ܽ���ź�������ս����
				for (int j = 1; j < listCell.size(); j++) {
					HashSet<String> set = new HashSet<>();
					if (VT.contains(listCell.get(j))) {
						// System.out.println(listCell.get(j - 1) + ":" + listCell.get(j));
						set.add(listCell.get(j));
						if (keyFollow.containsKey(listCell.get(j - 1)))
							set.addAll(keyFollow.get(listCell.get(j - 1)));
						keyFollow.put(listCell.get(j - 1), set);
					}
				}
				// (2)��...VnVn...���
				for (int j = 0; j < listCell.size() - 1; j++) {
					HashSet<String> set = new HashSet<>();
					if (VN.contains(listCell.get(j)) && VN.contains(listCell.get(j + 1))) {
						set.addAll(FIRST.get(listCell.get(j + 1)));
						set.remove("��");

						if (keyFollow.containsKey(listCell.get(j)))
							set.addAll(keyFollow.get(listCell.get(j)));
						keyFollow.put(listCell.get(j), set);
					}
				}

				// (3)A->...B ���� A->...B��(������n����)����ϴ�����
				for (int j = 0; j < listCell.size(); j++) {
					HashMap<String, String> vn_Vn;
					if (VN.contains(listCell.get(j)) && !listCell.get(j).equals(key)) {// ��VN��A������B
						boolean isAllNull = false;// ���VN���Ƿ�Ϊ��
						if (j + 1 < listCell.size())// ��A->...B��(������n����)
							for (int k = j + 1; k < listCell.size(); k++) {
								if ((FIRST.containsKey(listCell.get(k)) ? FIRST.get(listCell.get(k)).contains("��")
										: false)) {// ��������Ķ���VN����FIRST�а�����
									isAllNull = true;
								} else {
									isAllNull = false;
									break;
								}
							}
						// ��������һ��ΪVN,��A->...B
						if (j == listCell.size() - 1) {
							isAllNull = true;
						}
						if (isAllNull) {
							vn_VnListLeft.add(key);
							vn_VnListRight.add(listCell.get(j));

							// ��vn_VnList����ӣ��ִ��ںͲ������������
							boolean isHaveAdd = false;
							for (int x = 0; x < vn_VnList.size(); x++) {
								HashMap<String, String> vn_VnListCell = vn_VnList.get(x);
								if (!vn_VnListCell.containsKey(key)) {
									vn_VnListCell.put(key, listCell.get(j));
									vn_VnList.set(x, vn_VnListCell);
									isHaveAdd = true;
									break;
								} else {
									// ȥ��
									if (vn_VnListCell.get(key).equals(listCell.get(j))) {
										isHaveAdd = true;
										break;
									}
									continue;
								}
							}
							if (!isHaveAdd) {// ���û����ӣ���ʾ���µ����
								vn_Vn = new HashMap<>();
								vn_Vn.put(key, listCell.get(j));
								vn_VnList.add(vn_Vn);
							}
						}
					}
				}
			}
		}

		keyFollow.toString();

		// (4)vn_VnListLeft��ȥvn_VnListRight,ʣ�µľ�����ڲ���ʽ��
		vn_VnListLeft.removeAll(vn_VnListRight);
		Queue<String> keyQueue = new LinkedList<>();// ��ջ���߶��ж���
		Iterator<String> itVnVn = vn_VnListLeft.iterator();
		while (itVnVn.hasNext()) {
			keyQueue.add(itVnVn.next());
		}
		while (!keyQueue.isEmpty()) {
			String keyLeft = keyQueue.poll();
			for (int t = 0; t < vn_VnList.size(); t++) {
				HashMap<String, String> vn_VnListCell = vn_VnList.get(t);
				if (vn_VnListCell.containsKey(keyLeft)) {
					HashSet<String> set = new HashSet<>();
					// ԭ����FOLLOW������ߵ�FOLLOW
					if (keyFollow.containsKey(keyLeft))
						set.addAll(keyFollow.get(keyLeft));
					if (keyFollow.containsKey(vn_VnListCell.get(keyLeft)))
						set.addAll(keyFollow.get(vn_VnListCell.get(keyLeft)));
					keyFollow.put(vn_VnListCell.get(keyLeft), set);
					keyQueue.add(vn_VnListCell.get(keyLeft));

					// �Ƴ��Ѵ�������
					vn_VnListCell.remove(keyLeft);
					vn_VnList.set(t, vn_VnListCell);
				}
			}
		}

		// ��ʱkeyFollowΪ������FOLLOW��
		FOLLOW = keyFollow;
		// ��ӡFOLLOW����
		Iterator<String> itF = keyFollow.keySet().iterator();
		while (itF.hasNext()) {
			String key = itF.next();
			HashSet<String> f = keyFollow.get(key);
			System.out.println("\tFOLLOW(" + key + ")={" + String.join("��", f.toArray(new String[f.size()])) + "}");
		}
	}

	// ����ֱ����ݹ�
	private static void reformMap() {
		boolean isReForm = false;// MAP�Ƿ��޸�
		Set<String> keys = new HashSet<>();
		keys.addAll(MAP.keySet());
		Iterator<String> it = keys.iterator();
		ArrayList<String> nullSign = new ArrayList<>();
		nullSign.add("��");
		while (it.hasNext()) {
			String left = it.next();
			boolean flag = false;// �Ƿ�����ݹ�
			ArrayList<ArrayList<String>> rightList = MAP.get(left);
			ArrayList<String> oldRightCell = new ArrayList<>(); // �ɲ������ұ�
			ArrayList<ArrayList<String>> newLeftNew = new ArrayList<>();// ����µ���ߺ��µ��ұ�

			// ����ֱ����ݹ�
			for (int i = 0; i < rightList.size(); i++) {
				ArrayList<String> newRightCell = new ArrayList<>(); // �²���ʽ���ұ�
				if (rightList.get(i).get(0).equals(left)) {
					for (int j = 1; j < rightList.get(i).size(); j++) {
						newRightCell.add(rightList.get(i).get(j));
					}
					flag = true;
					newRightCell.add(left + "\'");
					newLeftNew.add(newRightCell);
				} else {
					for (int j = 0; j < rightList.get(i).size(); j++) {
						oldRightCell.add(rightList.get(i).get(j));
					}
					oldRightCell.add(left + "\'");
				}
			}
			if (flag) {// �������ݹ飬�����MAP
				isReForm = true;
				newLeftNew.add(nullSign);
				MAP.put(left + "\'", newLeftNew);
				VN.add(left + "\'"); // �����µ�VN
				VT.add("��"); // ����ŵ�VT
				ArrayList<ArrayList<String>> newLeftOld = new ArrayList<>();// ���ԭ�ȣ����ǲ����µ��ұ�
				newLeftOld.add(oldRightCell);
				MAP.put(left, newLeftOld);
			}
			// �����ݹ�
			// ��ȡ������
			// ����...

		}
		// ����ķ����޸ģ�������޸ĺ���ķ�
		if (isReForm) {
			System.out.println("�����ķ�����ݹ�:");
			Set<String> kSet = new HashSet<>(MAP.keySet());
			Iterator<String> itk = kSet.iterator();
			while (itk.hasNext()) {
				String k = itk.next();
				ArrayList<ArrayList<String>> leftList = MAP.get(k);
				System.out.print("\t" + k + "��");
				for (int i = 0; i < leftList.size(); i++) {
					System.out.print(String.join("", leftList.get(i).toArray(new String[leftList.get(i).size()])));
					if (i + 1 < leftList.size())
						System.out.print("|");
				}
				System.out.println();
			}
		}
		MAP.toString();

	}

	// ���ļ����ķ�
	public static ArrayList<String> readFile(File file) {
		System.out.println("���ļ�������ķ�Ϊ:");
		ArrayList<String> result = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;
			while ((s = br.readLine()) != null) {
				System.out.println("\t" + s);
				result.add(s.trim());
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
