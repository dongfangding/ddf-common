# ddf-common
通用包

# TODO
1.  以来的第三方组件中依赖的开源库的版本低于系统中自己使用的， 如leaf的guava和curator， guava.version版本暂时为了兼容leaf降级了
2. RestControllerAdvice实现的方式，如何使用配置实现，外部传入包路径

# 依赖问题
1. Leaf和elastic对zookeeper和guava使用的版本都不一致， guava由于兼容性很烂， 在各自模块中使用了不同版本的guava。至于zk框架curator由于elastic-job中使用的版本为5.1.0， 该版本不再支持3.4.x版本的z, 因此需要提高zk的安装版本