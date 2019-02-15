#include<stdio.h>
#include<pthread.h>
#include<semaphore.h>
#include<queue>
#include <zconf.h>
#include <ctime>


#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
using namespace std;


//semaphore to control sleep and wake up
sem_t emp;
sem_t full;
queue<int> q;


void init_semaphore() {
	sem_init(&emp, 0, 5);
	sem_init(&full, 0, 0);
}

void *ProducerFunc(void *arg) {
	printf("%s\n", (char *) arg);
	int i;
	for (i = 1; i <= 100; i++) {
		sem_wait(&emp);


//		sleep(1);

		q.push(i);
		printf("producer produced item %d\n", i);


		sem_post(&full);
	}
}

void *ConsumerFunc(void *arg) {
	printf("%s\n", (char *) arg);
	int i;
	for (i = 1; i <= 96; i++) {
		sem_wait(&full);

//		sleep(2);
//		nanosleep(5000);


		int item = q.front();
		q.pop();
		printf("consumer consumed item %d\n", item);


		sem_post(&emp);
	}
}


int main() {
	pthread_t thread1;
	pthread_t thread2;

	init_semaphore();

	char *message1 = const_cast<char *>("i am producer");
	char *message2 = const_cast<char *>("i am consumer");

	pthread_create(&thread1, nullptr, ProducerFunc, (void *) message1);
	pthread_create(&thread2, nullptr, ConsumerFunc, (void *) message2);

	pthread_join(thread1, nullptr);
	pthread_join(thread2, nullptr);
//	while (true);
	return 0;
}

#pragma clang diagnostic pop