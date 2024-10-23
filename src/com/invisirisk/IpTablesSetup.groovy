package com.invisirisk

// class NodeSetup {
class IpTablesSetup {
    // static void setup(script) {
    //     def containerName = env.PSE_CONTAINER_NAME
    //     script.sh '''
    //         echo "Installing dependencies"
    //         apk add iptables ca-certificates git curl
    //         echo "Setting up iptables"
    //         whoami
    //         iptables -t nat -N 192.168.88.57
    //         iptables -t nat -A OUTPUT -j 192.168.88.57
    //         echo "Setting up PSE"
    //         PSE_IP=$(getent hosts 192.168.88.57 | awk '{ print $1 }')
    //         echo "PSE_IP is ${PSE_IP}"
    //         iptables -t nat -A pse -p tcp -m tcp --dport 443 -j DNAT --to-destination ${PSE_IP}:12345
    //     '''
    // }
    static void setup(script) {
    script.sh """
        def containerName = env.PSE_CONTAINER_NAME
        echo "Installing dependencies"
        apk add iptables ca-certificates git curl
        echo "Setting up iptables"
        iptables -t nat -N ${env.PSE_CONTAINER_NAME}
        iptables -t nat -A OUTPUT -j ${env.PSE_CONTAINER_NAME}
        echo "Setting up PSE"
        PSE_IP=\$(getent hosts ${env.PSE_CONTAINER_NAME} | awk '{ print \$1 }')
        echo "PSE_IP is \${PSE_IP}"
        iptables -t nat -A ${env.PSE_CONTAINER_NAME} -p tcp -m tcp --dport 443 -j DNAT --to-destination \${PSE_IP}:12345
    """
}
}