package com.invisirisk

class NodeSetup {
    static void setup() {
        sh '''
            apk add iptables ca-certificates git curl
            iptables -t nat -N pse
            iptables -t nat -A OUTPUT -j pse
            PSE_IP=$(getent hosts pse | awk '{ print $1 }')
            iptables -t nat -A pse -p tcp -m tcp --dport 443 -j DNAT --to-destination ${PSE_IP}:12345
        '''
    }
}
