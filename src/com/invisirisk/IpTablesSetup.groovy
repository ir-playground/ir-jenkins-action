package com.invisirisk

// class NodeSetup {
class IpTablesSetup {
    static void setup(script) {
        def containerName = script.env.PSE_CONTAINER_NAME

        script.sh """
            echo "Installing dependencies"
            apk add iptables ca-certificates git curl
            echo "Setting up iptables"
            iptables -t nat -N ${containerName}
            iptables -t nat -A OUTPUT -j ${containerName}
            echo "Setting up PSE"
            PSE_IP=\$(getent hosts ${containerName} | awk '{ print \$1 }')
            echo "PSE_IP is \${PSE_IP}"
            iptables -t nat -A ${containerName} -p tcp -m tcp --dport 443 -j DNAT --to-destination \${PSE_IP}:12345
        """
    }
}
