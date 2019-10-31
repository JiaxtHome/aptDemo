package com.xibeixue.apt_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.xibeixue.apt_annotation.BindViewTo;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Elements mElementUtils;
    private HashMap<String, BinderClassCreator> mCreatorMap = new HashMap<>();

    /**
     * init方法一般用于初始化一些用到的工具类，主要有
     * processingEnvironment.getElementUtils(); 处理Element的工具类，用于获取程序的元素，例如包、类、方法。
     * processingEnvironment.getTypeUtils(); 处理TypeMirror的工具类，用于取类信息
     * processingEnvironment.getFiler(); 文件工具
     * processingEnvironment.getMessager(); 错误处理工具
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtils = processingEnv.getElementUtils();
    }

    /**
     * 获取Java版本，一般用最新版本
     * 也可以使用注解方式：@SupportedSourceVersion(SourceVersion.RELEASE_7)
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 获取目标注解列表
     * 也可以使用注解方式：@SupportedAnnotationTypes("com.xibeixue.apt_annotation.BindViewTo")
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(BindViewTo.class.getCanonicalName());
        return supportTypes;
    }

    /**
     * 编译期回调方法,apt核心实现方法
     * 包含所有使用目标注解的元素(Element)
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //扫描整个工程, 找出所有使用BindViewTo注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindViewTo.class);
        //遍历元素, 为每一个类元素创建一个Creator
        for (Element element : elements) {
            //BindViewTo限定了只能属性使用, 这里强转为变量元素VariableElement
            VariableElement variableElement = (VariableElement) element;
            //获取封装属性元素的类元素TypeElement
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            //获取简单类名
            String fullClassName = classElement.getQualifiedName().toString();
            BinderClassCreator creator = mCreatorMap.get(fullClassName);
            //如果不存在, 则创建一个对应的Creator
            if (creator == null) {
                creator = new BinderClassCreator(mElementUtils.getPackageOf(classElement), classElement);
                mCreatorMap.put(fullClassName, creator);

            }
            //将需要绑定的变量和对应的view id存储到对应的Creator中
            BindViewTo bindAnnotation = variableElement.getAnnotation(BindViewTo.class);
            int id = bindAnnotation.value();
            creator.putElement(id, variableElement);
        }

        //每一个类将生成一个新的java文件，其中包含绑定代码
        for (String key : mCreatorMap.keySet()) {
            BinderClassCreator binderClassCreator = mCreatorMap.get(key);
            //通过javapoet构建生成Java类文件
            JavaFile javaFile = JavaFile.builder(binderClassCreator.getPackageName(),
                    binderClassCreator.generateJavaCode()).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }
}


