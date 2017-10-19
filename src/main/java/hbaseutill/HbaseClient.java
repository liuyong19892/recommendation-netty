package hbaseutill;


import md5.MD5Utils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class HbaseClient {
    public static Configuration configuration;
    public static Connection connection;
    public static Admin admin;

    public static void main(String[] args) throws IOException {
        scanData("test","1","10");
    }

    // 初始化链接
    public static void init() throws IOException{
        configuration = HBaseConfiguration.create();
        /*
         * configuration.set("hbase.zookeeper.quorum",
         * "10.10.3.181,10.10.3.182,10.10.3.183");
         * configuration.set("hbase.zookeeper.property.clientPort","2181");
         * configuration.set("zookeeper.znode.parent","/hbase");
         */
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.zookeeper.quorum", "master,node1,node2,node3");
        configuration.set("hbase.master", "master:60000");
        try {
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 关闭连接
    public static void close() {
        try {
            if (null != admin)
                admin.close();
            if (null != connection)
                connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 建表
    public static void createTable(String tableNmae, String[] cols) throws IOException {

        init();
        TableName tableName = TableName.valueOf(tableNmae);

        if (admin.tableExists(tableName)) {
            System.out.println("talbe is exists!");
        } else {
            HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
            for (String col : cols) {
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(col);
                hTableDescriptor.addFamily(hColumnDescriptor);
            }
            admin.createTable(hTableDescriptor);
        }
        close();
    }

    // 删表
    public static void deleteTable(String tableName) throws IOException {
        init();
        TableName tn = TableName.valueOf(tableName);
        if (admin.tableExists(tn)) {
            admin.disableTable(tn);
            admin.deleteTable(tn);
        }
        close();
    }

    // 查看已有表
    public static void listTables() throws IOException {
        init();
        HTableDescriptor hTableDescriptors[] = admin.listTables();
        for (HTableDescriptor hTableDescriptor : hTableDescriptors) {
            System.out.println(hTableDescriptor.getNameAsString());
        }
        close();
    }
    // 插入数据
    public static void insterRow(String tableName, String rowkey, String colFamily, String col, String val)
            throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowkey));
        put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col), Bytes.toBytes(val));
        table.put(put);

        // 批量插入
        /*
         * List<Put> putList = new ArrayList<Put>(); puts.add(put);
         * table.put(putList);
         */
        table.close();
        close();
    }

    // 删除数据
    public static void deleRow(String tableName, String rowkey, String colFamily, String col) throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowkey));
        // 删除指定列族
        // delete.addFamily(Bytes.toBytes(colFamily));
        // 删除指定列
        // delete.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        table.delete(delete);
        // 批量删除
        /*
         * List<Delete> deleteList = new ArrayList<Delete>();
         * deleteList.add(delete); table.delete(deleteList);
         */
        table.close();
        close();
    }

    // 根据rowkey查找数据
    public static String getData(String rowkey, String tableName) throws IOException{
        Table table = null;
        try {
           table = connection.getTable(TableName.valueOf(tableName));
           Get get = new Get(Bytes.toBytes(MD5Utils.MD5(rowkey)));
           Result result = table.get(get);
           Cell[] cells = result.rawCells();
            return new String(CellUtil.cloneValue(cells[0]));
        } catch (Exception e) {
            e.printStackTrace();return "";
        } finally {
            if(table!=null) table.close();
        }
    }

    // 格式化输出
    public static void showCell(Result result) {
        Cell[] cells = result.rawCells();
        for (Cell cell : cells) {
            System.out.println("RowName:" + new String(CellUtil.cloneRow(cell)) + " ");
            System.out.println("Timetamp:" + cell.getTimestamp() + " ");
            System.out.println("column Family:" + new String(CellUtil.cloneFamily(cell)) + " ");
            System.out.println("row Name:" + new String(CellUtil.cloneQualifier(cell)) + " ");
            System.out.println("value:" + new String(CellUtil.cloneValue(cell)) + " ");
        }
    }

    // 批量查找数据
    public static void scanData(String tableName, String startRow, String stopRow) throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        // scan.setStartRow(Bytes.toBytes(startRow));
        // scan.setStopRow(Bytes.toBytes(stopRow));
        ResultScanner resultScanner = table.getScanner(scan);
        for (Result result : resultScanner) {
            showCell(result);
        }
        table.close();
        close();
    }

}
