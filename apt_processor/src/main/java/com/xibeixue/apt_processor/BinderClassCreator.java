package com.xibeixue.apt_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class BinderClassCreator {

    public static final String ParamName = "rootView";

    private TypeElement mTypeElement;
    private String mPackageName;
    private String mBinderClassName;
    private Map<Integer, VariableElement> mVariableElements = new HashMap<>();

    /**
     * @param packageElement 包元素
     * @param classElement   类元素
     */
    public BinderClassCreator(PackageElement packageElement, TypeElement classElement) {
        this.mTypeElement = classElement;
        mPackageName = packageElement.getQualifiedName().toString();
        mBinderClassName = classElement.getSimpleName().toString() + "_ViewBinding";
    }

    public void putElement(int id, VariableElement variableElement) {
        mVariableElements.put(id, variableElement);
    }

    public TypeSpec generateJavaCode() {
        return TypeSpec.classBuilder(mBinderClassName)
                //public 修饰类
                .addModifiers(Modifier.PUBLIC)
                //添加类的方法
                .addMethod(generateMethod())
                //构建Java类
                .build();

    }

    private MethodSpec generateMethod() {
        //获取全类名
        ClassName className = ClassName.bestGuess(mTypeElement.getQualifiedName().toString());
        //构建方法--方法名
        return MethodSpec.methodBuilder("bindView")
                //public方法
                .addModifiers(Modifier.PUBLIC)
                //返回void
                .returns(void.class)
                //方法传参（参数全类名，参数名）
                .addParameter(className, ParamName)
                //方法代码
                .addCode(generateMethodCode())
                .build();
    }

    private String generateMethodCode() {
        StringBuilder code = new StringBuilder();
        for (int id : mVariableElements.keySet()) {
            VariableElement variableElement = mVariableElements.get(id);
            //变量名称
            String name = variableElement.getSimpleName().toString();
            //变量类型
            String type = variableElement.asType().toString();
            //rootView.name = (type)view.findViewById(id), 注意原类中变量声明不能为private，否则这里是获取不到的
            String findViewCode = ParamName + "." + name + "=(" + type + ")" + ParamName + ".findViewById(" + id + ");\n";
            code.append(findViewCode);

        }
        return code.toString();
    }

    public String getPackageName() {
        return mPackageName;
    }
}
