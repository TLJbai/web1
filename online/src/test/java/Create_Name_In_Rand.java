public class Create_Name_In_Rand {
    public static void main(String[] args) {
        System.out.println("随间产生一个名字：");
        for (int i = 0; i < 10; i++) {
            System.out.print("第"+i+"个："+ "蜂友"+((Math.random())+"").substring(2,6));
            System.out.println();
        }
    }
}
