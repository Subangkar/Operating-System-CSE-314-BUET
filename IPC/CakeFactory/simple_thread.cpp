#include <iostream>
#include<stdio.h>
#include<pthread.h>
#include<stdlib.h>
#include <zconf.h>

using namespace std;


void *threadFunc1(void *arg) {
	int i;
	for (i = 1; i <= 5; i++) {
		printf("%d %s\n", i, (char *) arg);
		sleep(1);
	}
}

void *threadFunc2(void *arg) {
	int i;
	for (i = 1; i <= 5; i++) {
		printf("%d %s\n", i, (char *) arg);
		sleep(1);
	}
}


int main(void) {
	pthread_t thread1;
	pthread_t thread2;

	char *message1 = "i am thread 1";
	char *message2 = "i am thread 2";

	pthread_create(&thread1, nullptr, threadFunc1, (void *) message1);
	pthread_create(&thread2, nullptr, threadFunc2, (void *) message2);

	pthread_join(thread1, nullptr);
	pthread_join(thread2, nullptr);
//	while (true);
	return 0;
}
