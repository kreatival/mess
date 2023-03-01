#!/bin/bash

. ./lib.sh

SERVER_DOMAIN=$1

sed -i "s/=shadow/=${USER_SH}/" coturn.service
cp coturn.service /etc/systemd/system/

printf "\nInstalling necessary packages..."

dnf -y install openssl-devel libevent libevent-devel hiredis hiredis-devel gcc make

cd ${USER_PATH}

echo "Downloading Coturn.."

wget https://coturn.net/turnserver/v4.5.1.3/turnserver-4.5.1.3.tar.gz

echo "Unpacking..."

tar -zxf turnserver-4.5.1.3.tar.gz

cd turnserver-4.5.1.3

echo "Creating makefile..."

./configure

echo "Installing..."

make
make install

# Setting up the configuration file

echo "Tuning configuration..."

cp /usr/local/etc/turnserver.conf.default /usr/local/etc/turnserver.conf

read -p "Is your server behind NAT [y/n]?" -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]
then
    printf "\n"
    echo "Enter the outside IP address of your server >>"
    read NAT_SERVER_EXTERNAL_IP
    sed -i "s/^#external-ip=60.70.80.92\/172.17.19.102/external-ip=${NAT_SERVER_EXTERNAL_IP}/" /usr/local/etc/turnserver.conf
    
    printf "\n"
    echo "Enter the inside IP address of your server >>"
    read NAT_SERVER_IP
    sed -i "s/^#listening-ip=172.17.19.101/listening-ip=${NAT_SERVER_IP}/" /usr/local/etc/turnserver.conf 
else
    printf "\nEnter the IP address of your server >>"
    read NAT_SERVER_IP
    sed -i "s/^#listening-ip=172.17.19.101/listening-ip=${NAT_SERVER_IP}/" /usr/local/etc/turnserver.conf   
fi

read -p "Do you want to enable STUN (this will make your installation a public STUN server!) [y/n]?" -n 1 -r
if [[ $REPLY =~ ^[Nn]$ ]]
then    
    sed -i "s/^#no-stun/no-stun/" /usr/local/etc/turnserver.conf
    sed -i "s/- stun/# - stun/" ${SERVER_PATH}/config/shadow.yml
else
    if test -f ${SERVER_PATH}/config/shadow.yml
    then      
       sed -i "s/stun\:shadow.example.com/stun\:${SERVER_DOMAIN}/" ${SERVER_PATH}/config/shadow.yml
    fi       
fi

sed -i "s/^#use-auth-secret/use-auth-secret/" /usr/local/etc/turnserver.conf

printf "\nEnter the shared secret for the TURN authorization (should match that configured in the Shadow server) >>"
    read -r TURN_AUTH_SECRET
    
    if [ -z "$TURN_AUTH_SECRET" ]
    then 
        error_quit "Entered secret is empty"
    fi
    
# Update config
    
    TURN_AUTH_SECRET_CONV=$(preproc_sed $(normalize_turn $(preproc_cfg $TURN_AUTH_SECRET)))
        
    sed -i "s/^#static-auth-secret=north/static-auth-secret=${TURN_AUTH_SECRET_CONV}/" /usr/local/etc/turnserver.conf
    
    if test -f ${SERVER_PATH}/config/shadow.yml
    then
       TURN_AUTH_SECRET_CONV2=$(preproc_sed $(normalize_yaml $(preproc_cfg $TURN_AUTH_SECRET)))        
       sed -i "s/secret: your_turn_secret/secret\: '${TURN_AUTH_SECRET_CONV2}'/" ${SERVER_PATH}/config/shadow.yml
       sed -i "s/turn\:shadow.example.com/turn\:${SERVER_DOMAIN}/" ${SERVER_PATH}/config/shadow.yml
    fi

# sed -i "s/^#redis-userdb=\"ip=<ip-address> dbname=<database-number> password=<database-user-password> port=<port> connect_timeout=<seconds>\"/redis-userdb=\"ip=127.0.0.1 port=6379\"/" /usr/local/etc/turnserver.conf
sed -i "s/^#realm=mycompany.org/realm=${SERVER_DOMAIN}/" /usr/local/etc/turnserver.conf
sed -i "s/^#no-tcp/no-tcp/" /usr/local/etc/turnserver.conf
sed -i "s/^#no-multicast-peers/no-multicast-peers/" /usr/local/etc/turnserver.conf
sed -i "s/^#pidfile=\"\/var\/run\/turnserver.pid\"/pidfile=\"\/var\/tmp\/coturn.pid\"/" /usr/local/etc/turnserver.conf

read -p "Do you want to enable CLI access for Coturn [y/n]?" -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]
then  
    printf "\nEnter the password to be used for CLI access >>"
    read -r CLI_PASSWORD
    
    if [ -z "$CLI_PASSWORD" ]
    then 
        error_quit "Entered password is empty"
    fi     
    
    CLI_PASSWORD_CONV=$(preproc_sed $(normalize_turn $(preproc_cfg $CLI_PASSWORD)))
    
    sed -i "s/^#cli-password=qwerty/cli-password=${CLI_PASSWORD_CONV}/" /usr/local/etc/turnserver.conf
    
    echo "Installing telnet..."
    dnf -y install telnet  
fi

read -p "Do you want to set up custom media port range for Coturn (default is 49152 to 65535) [y/n]?" -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]
then  
    printf "\nEnter the first port of the range >>"
    read STARTING_PORT    
    sed -i "s/^#min-port=49152/min-port=${STARTING_PORT}/" /usr/local/etc/turnserver.conf
    
    printf "\nEnter the last port of the range >>"
    read ENDING_PORT    
    sed -i "s/^#max-port=65535/max-port=${ENDING_PORT}/" /usr/local/etc/turnserver.conf
    
    echo "Opening ports..."
    
    firewall-cmd --zone=public --permanent --add-port=3478/udp
    firewall-cmd --zone=public --permanent --add-port=$STARTING_PORT-$ENDING_PORT/udp
    firewall-cmd --reload
else
    echo "Opening ports..."
    
    firewall-cmd --zone=public --permanent --add-port=3478/udp
    firewall-cmd --zone=public --permanent --add-port=49152-65535/udp
    firewall-cmd --reload  
      
fi

echo "Creating Coturn service..."

systemctl daemon-reload
systemctl enable coturn.service

echo "Starting Coturn service..."

systemctl start coturn.service
    