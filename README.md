# Socket Delivery Server

部署步骤：
1. Spring Cloud Consul服务注册和发现，这里主要参考：
https://blog.csdn.net/it_lihongmin/article/details/91357445
2. 在集群Leader服务器上部署delivery-demo；
3. socket-demo是需要部署到多台服务器上的，修改application.property参数后打包放到要部署的服务器上，启动即可；

调用方法：
1. 设备每次连接的时候，先通过socket请求到delivery-demo，然后delivery的netty服务端会发送可用的一台socket server服务端机器的信息，然后端口socket连接；
2. 设备拿到socket server的服务器信息后，进行socket连接（长连接）通讯；
