
/** 父module先于子module初始化，无法引用子module方法 */

public class Parent {

    public void parent(String[] args) {
        System.out.println("父module先于子module初始化，无法引用子module方法");
    }
}