job("kube1_groovy"){
  description("kubernetes job1")
  scm{
    github('VanshitaMittal/Task6','master')
  }
  steps{
    shell('sudo cp -vrf * /task6/jenkins')
  }
  triggers {
        		upstream('SeedJob', 'SUCCESS')
    			}
	triggers{
    gitHubPushTrigger()
  }
	
}


job("kube2_groovy"){
  steps{
    shell('''
	if sudo ls /task6/jenkins | grep php
      	then
		if sudo kubectl get deployment --selector "app in (httpd)" | grep httpd-web
    		then
			sudo kubectl apply -f /task6/jenkins/deploy.yml
           		POD=$(sudo kubectl get pod -l app=httpd -o jsonpath="{.items[0].metadata.name}")
        		echo $POD
        		sudo kubectl cp /task6/jenkins/index.php $POD:/var/www/html
		else
    			
        		sudo kubectl create -f /task6/jenkins/deploy.yml
        		POD=$(sudo kubectl get pod -l app=httpd -o jsonpath="{.items[0].metadata.name}")
        		echo $POD
        		sudo kubectl cp /task6/jenkins/index.php $POD:/var/www/html/index.php
    		fi
   	fi
	''')
  }
  triggers {
        upstream('kube1_groovy', 'SUCCESS')
  }
}

job("kube3_groovy")
{
  steps{
    shell('''
status=$(curl -o /dev/null -s -w "%{http_code}" http://192.168.99.101:30002)
if [[ $status == 200 ]]
then
    echo "Running"
    exit 0
else
     exit 1
fi
     ''')
  }
  
  triggers {
        upstream('kube2_groovy', 'SUCCESS')
  }
  
  publishers {
        extendedEmail {
            recipientList('study6526@gmail.com')
            defaultSubject('Job status')
          	attachBuildLog(attachBuildLog = true)
            defaultContent('Status Report')
            contentType('text/html')
            triggers {
                always {
                    subject('build Status')
                    content('Body')
                    sendTo {
                        developers()
                        recipientList()
                    }
		}
	    }
	}
    }
}
