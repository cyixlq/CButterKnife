# CButterKnife
疯狂地造轮子系列——CButterKnife，实现思路与原版大同小异，但是对比原版真的是太简陋。

## 特点
androidX：支持（未测试，理论可以）

module中的控件绑定：不支持

支持的注解（相较于原版）：只支持@BindView和@OnClick

## 用法
依赖引入：
```
implementation project(path: ':cbutterknife')
annotationProcessor project(path: ':compiler')
```

与原版基本一致，只不过只支持两个注解@BindView和@OnClick。绑定代码只是名称变了：
```
CButterKnife.bind(this);        //Activity中绑定
CButterKnife.bind(this, view);  //Fragment中绑定
```
不需要解绑（unBind）

## 后记
实现思路基本一致，但是写法与原版出入太大，毕竟原本太健壮了。这个只是为了自己动手实现一下 给自己增加成就感，写的一个简陋版本。