
/** 父module先于子module初始化，无法引用子module方法 */

public class MainStart {

    public static void main(String[] args) {
        System.out.println("父module先于子module初始化，无法引用子module方法");
    }
}