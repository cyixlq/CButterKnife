package top.cyixlq.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import top.cyixlq.annotation.BindView;
import top.cyixlq.annotation.OnClick;

@AutoService(Processor.class)
public class CButterKnifeProcessor extends AbstractProcessor {

    private static final String VIEW_BINDING = "$ViewBinding";

    private Filer filer;
    private Elements elementUtils;
    private Messager messager;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName());
        set.add(OnClick.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Map<String, List<Element>> map = new HashMap<>();

        // 开始收集所有类所有注解过的节点
        Set<? extends Element> bindViewElements = roundEnv.getElementsAnnotatedWith(BindView.class);
        Set<? extends Element> onClickElements = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element bindViewElement : bindViewElements) {
            final String clazzName = getClassName(bindViewElement);
            List<Element> elements = map.get(clazzName);
            if (elements == null) {
                elements = new ArrayList<>();
                map.put(clazzName, elements);
            }
            elements.add(bindViewElement);
        }
        for (Element onClickElement : onClickElements) {
            final String clazzName = getClassName(onClickElement);
            List<Element> elements = map.get(clazzName);
            if (elements == null) {
                elements = new ArrayList<>();
                map.put(clazzName, elements);
            }
            elements.add(onClickElement);
        }

        if (map.isEmpty()) return false;

        Set<String> keySet = map.keySet();
        for (String clazzName : keySet) {
            List<Element> elements = map.get(clazzName);
            if (elements == null || elements.isEmpty()) continue;
            final String packageName = getPackageName(elements.get(0));
            // 创建构造方法进行控件绑定
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addParameter(ClassName.get(packageName, clazzName), "target", Modifier.FINAL)
                    .addParameter(ClassName.get("android.view", "View"), "view")
                    .addModifiers(Modifier.PUBLIC);
            for (Element element : elements) {
                if (element instanceof VariableElement) { // 如果是属性节点
                    // 被BindView注解的属性
                    final VariableElement variableElement = (VariableElement) element;
                    // 获取BindView注解的id值
                    final int id = variableElement.getAnnotation(BindView.class).value();
                    constructorBuilder.addStatement("target.$L = view.findViewById($L)", variableElement, id);
                } else if (element instanceof ExecutableElement) {
                    final ExecutableElement executableElement = (ExecutableElement) element;
                    List<? extends VariableElement> parameters = executableElement.getParameters();
                    if (parameters != null && parameters.size() > 1) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "@OnClick The method can only have one parameter at most. ");
                        continue;
                    }
                    int[] value = executableElement.getAnnotation(OnClick.class).value();
                    for (int id : value) {
                        constructorBuilder.addCode("view.findViewById($L).setOnClickListener(new View.OnClickListener() {\n", id);
                        constructorBuilder.addCode("\tpublic void onClick(View v) {\n");
                        if (parameters == null || parameters.isEmpty()) {
                            constructorBuilder.addStatement("\t\ttarget.$L", executableElement);
                        } else {
                            VariableElement variableElement = parameters.get(0);
                            // 如果方法中参数是View
                            if (isTypeEqual(variableElement.asType(), "android.view.View")) {
                                constructorBuilder.addStatement("\t\ttarget.$L(v)", executableElement.getSimpleName());
                            } else if(isSubtypeOfType(variableElement.asType(), "android.view.View")) {
                                // 如果方法中参数是View的子类，那么需要强转
                                constructorBuilder.addStatement("\t\ttarget.$L(($L)v)",
                                        executableElement.getSimpleName(), variableElement.asType());
                            } else {
                                messager.printMessage(Diagnostic.Kind.ERROR,
                                        "@OnClick The parameters in the method can only be subclasses of View.");
                            }
                        }
                        constructorBuilder.addCode("\t}\n");
                        constructorBuilder.addStatement("})");
                    }
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Unknown annotation");
                }
            }
            TypeSpec viewBinding = TypeSpec.classBuilder(clazzName + VIEW_BINDING)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(constructorBuilder.build())
                    .build();
            try {
                JavaFile javaFile = JavaFile.builder(packageName, viewBinding).build();
                javaFile.writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.ERROR, "create java file failed: " + e.getLocalizedMessage());
            }
        }
        return true;
    }

    /**
     * 返回类名
     * @param element 传入TypeElement的子节点
     * @return 返回类名，不包含包名
     */
    private String getClassName(Element element) {
        return element.getEnclosingElement().getSimpleName().toString();
    }

    /**
     * 返回包名
     * @param element 传入TypeElement或其子节点
     * @return 返回包名，最后不包含小数点
     */
    private String getPackageName(Element element) {
        return elementUtils.getPackageOf(element).toString();
    }

    /**
     *  是否是某个类的子类
     * @param typeMirror 当前类
     * @param otherType 要对比的类
     * @return 当前类是否是对比类的子类或者实现类
     */
    private boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    /**
     *  当前类和对比类是否是同一类
     * @param typeMirror 当前类
     * @param otherType 对比类
     * @return 当前类和对比类是否是同一类
     */
    private boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }
}
