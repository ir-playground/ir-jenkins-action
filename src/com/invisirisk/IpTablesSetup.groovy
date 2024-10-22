package com.invisirisk

class NodeSetup {
    static void setup(script) {
        script.sh '''
            echo "Installing dependencies"
            apk add iptables ca-certificates git curl
            echo "Setting up iptables"
            iptables -t nat -N pse
            iptables -t nat -A OUTPUT -j pse
            echo "Setting up PSE"
            PSE_IP=$(getent hosts pse | awk '{ print $1 }')
            echo "PSE_IP is ${PSE_IP}"
            iptables -t nat -A pse -p tcp -m tcp --dport 443 -j DNAT --to-destination ${PSE_IP}:12345
        '''
    }
}
