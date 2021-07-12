import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

// Node内部类
class Node<K,V>{
    public int node_level;
    private K key;
    private V value;

    //Node数组，用于保存指向不同层级下一个节点的指针
    Node[] forward;

    public Node( K key, V value , int level) {
        this.node_level = level;
        this.key = key;
        this.value = value;
        // level + 1, because array index is from 0 - level
        this.forward = new Node [level+1];
        // Fill forward array with 0(NULL)
        Arrays.fill(forward, 0, level + 1, null);
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

}

//跳表
public class SkipList<K,V>{
    public SkipList(int max_level) {
        this._max_level = max_level;
        this._skip_list_level = 0;
        this._element_count = 0;
        // create header node and initialize key and value to null
        this._header = new Node<K, V>(null, null, _max_level);
    }

    // 跳表最大层数
    private int _max_level;

    // 跳表当前层数
    private int _skip_list_level;

    // 指向头节点
    Node<K, V> _header;

    // 跳表元素个数
    int _element_count;

    Node<K, V> create_node(K k, V v, int level) {
        Node<K, V> n = new Node<K, V>(k, v, level);
        return n;
    }

    int insert_element(K key, V value) {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();

        Node<K, V> current = this._header;

        // create update array and initialize it
        // update是一个数组，该数组放置了node.forward[i](不同层级的下一个节点数据)以后应该操作的节点
        Node<K, V>[] update = new Node[_max_level+1];
        Arrays.fill(update, 0, _max_level + 1, null);

        // 从跳表的当前层数开始,一层一层往下跳，找到合适的插入位置
        for(int i = _skip_list_level; i >= 0; i--) {
            while(current.forward[i] != null && String.valueOf(current.forward[i].getKey()).compareTo(String.valueOf(key)) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        // reached level 0 and forward pointer to right node, which is desired to insert key.
        current = current.forward[0];

        // if current node have key equal to searched key, we get it
        if (current != null && current.getKey() == key) {
            System.out.println("key: " + key + ", exists!");
            reentrantLock.unlock();
            return 1;
        }

        // if current is NULL that means we have reached to end of the level
        // if current's key is not equal to key that means we have to insert node between update[0] and current node
        if (current == null || current.getKey() != key ) {

            // Generate a random level for node
            int random_level = get_random_level();

            //如果随机层数大于跳表的当前层数，则使用指向头节点的指针初始化更新数组
            if (random_level > _skip_list_level) {
                for (int i = _skip_list_level + 1; i < random_level+1; i++) {
                    update[i] = _header;
                }
                _skip_list_level = random_level;
            }

            // create new node with random level generated
            Node<K, V> inserted_node = create_node(key, value, random_level);

            // insert node
            for (int i = 0; i <= random_level; i++) {
                inserted_node.forward[i] = update[i].forward[i];
                update[i].forward[i] = inserted_node;
            }
            System.out.println("Successfully inserted key:" + key + ", value:" + value);
            _element_count ++;
        }
        reentrantLock.unlock();
        return 0;
    }

    void dump_file() {
        System.out.println("dump_file-----------------");
        try {
            File writeName = new File("F:\\Code\\My-Skiplist-Java\\src\\main\\resources\\data.txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
            if(!writeName.exists()) {
                writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            }
            FileWriter writer = new FileWriter(writeName);
            BufferedWriter out = new BufferedWriter(writer);
            Node<K, V> node = this._header.forward[0];
            while (node != null) {
                out.write(node.getKey() + ":" + node.getValue());
                out.newLine();
                System.out.println(node.getKey() + ":" + node.getValue() + "; ");
                node = node.forward[0];
            }
            out.flush(); // 把缓存区内容压入文件
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void load_file() {
        System.out.println("load_file-----------------");
        String line, key, value;
        String pathname  = "F:\\Code\\My-Skiplist-Java\\src\\main\\resources\\data.txt";
        try{
            FileReader reader = new FileReader(pathname);
            BufferedReader br = new BufferedReader(reader);
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                if(!is_valid_string(line)) {
                    return;
                }
                key = line.substring(0, line.indexOf(":"));
                value = line.substring(line.indexOf(":") + 1, line.length());
                if (key.length() < 1 || value.length() < 1) {
                    continue;
                }
                insert_element((K)key, (V)value);
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void display_list() {
        System.out.println("*****Skip List*****");
        for (int i = 0; i <= _skip_list_level; i++) {
            Node<K, V> node = this._header.forward[i];
            System.out.print("Level " + i + ": ");
            while (node != null) {
                System.out.print(node.getKey() + ":" + node.getValue() + "; ");
                node = node.forward[i];
            }
            System.out.println();
        }
    }

    int size() {
        return _element_count;
    }

    Boolean is_valid_string(String str) {
        if (str.length() < 1) {
            return false;
        }
        if (str.indexOf(":") == -1) {
            return false;
        }
        return true;
    }

    void delete_element(K key) {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        Node<K, V> current = this._header;
        Node<K, V>[] update = new Node[_max_level+1];
        Arrays.fill(update, 0, _max_level + 1, null);

        // start from highest level of skip list
        for(int i = _skip_list_level; i >= 0; i--) {
            while(current.forward[i] != null && String.valueOf(current.forward[i].getKey()).compareTo(String.valueOf(key)) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        current = current.forward[0];
        if (current != null && current.getKey() == key) {

            // start for lowest level and delete the current node of each level
            for (int i = 0; i <= _skip_list_level; i++) {

                // if at level i, next node is not target node, break the loop.
                if (update[i].forward[i] != current)
                    break;

                update[i].forward[i] = current.forward[i];
            }

            // Remove levels which have no elements
            while (_skip_list_level > 0 && _header.forward[_skip_list_level] == null) {
                _skip_list_level --;
            }

            System.out.println("Successfully deleted key :" + key);
            _element_count --;
        }
        reentrantLock.unlock();
    }

    Boolean search_element(K key) {
        System.out.println("search_element-----------------");
        Node<K, V> current = _header;

        // start from highest level of skip list
        for (int i = _skip_list_level; i >= 0; i--) {
            while (current.forward[i] != null && String.valueOf(current.forward[i].getKey()).compareTo(String.valueOf(key)) < 0) {
                current = current.forward[i];
            }
        }

        //reached level 0 and advance pointer to right node, which we search
        current = current.forward[0];

        // if current node have key equal to searched key, we get it
        if (current != null && String.valueOf(current.getKey()).compareTo(String.valueOf(key)) == 0) {
            System.out.println("Found key: " + key + ", value: " + current.getValue());
            return true;
        }
        System.out.println("Not Found Key:" + key);
        return false;
    }

    int get_random_level(){
        int level = 1;
        Random random = new Random();
        while (random.nextInt(3) > 0) {
            level++;
        }
        //源码的随机层数
//        while(Math.random() < 0.25 && level < 32){
//            level++;
//        }
        level = (level < _max_level) ? level : _max_level;
        return level;
    };

}
