import java.util.Random;

public class Main {
    public static void main(String[] args) {
        SkipList skipList = new SkipList(6);
        skipList.insert_element("1", "xzs");
        skipList.display_list();
        skipList.insert_element("3", "xxzs");
        skipList.display_list();
        skipList.insert_element("7", "xxxzs");
        skipList.display_list();
        skipList.insert_element("8", "xxxxzs");
        skipList.display_list();
        skipList.insert_element("9", "xxxxxzs");
        skipList.display_list();
        skipList.insert_element("19", "xxxxxxxzs");
        skipList.display_list();
        skipList.insert_element("19", "xxxxxxxzs");
        skipList.display_list();

        System.out.println("skipList size:" + skipList.size());

        //skipList.dump_file();

        //skipList.load_file();

        skipList.search_element("9");
        skipList.search_element("18");

        skipList.display_list();

        System.out.println("skipList size:" + skipList.size());

        skipList.delete_element("3");
        skipList.delete_element("7");

        System.out.println("skipList size:" + skipList.size());

        skipList.display_list();
    }
}
