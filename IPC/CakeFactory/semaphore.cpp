#include<stdio.h>
#include<pthread.h>
#include<semaphore.h>
#include <zconf.h>

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
sem_t bin_sem;
pthread_mutex_t mtx;
char message[100];


void *thread_function(void *arg) {
	int x;
	char message2[10];
	while (1) {
		printf("thread2:waiting..\n");
		sem_wait(&bin_sem);
		printf("hi i am the new thread waiting inside critical..\n");
		scanf("%s", message);
		printf("You entered:%s\n", message);
		sem_post(&bin_sem);
		//pthread_mutex_unlock(&mtx);

	}
#pragma clang diagnostic pop

}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
int main() {
	pthread_t athread;
	pthread_attr_t ta;
	char message2[10];
	int x;
	sem_init(&bin_sem, 0, 1);
	pthread_mutex_init(&mtx, nullptr);

	pthread_attr_init(&ta);
	pthread_attr_setschedpolicy(&ta, SCHED_RR);

	pthread_create(&athread, &ta, thread_function, nullptr);
	pthread_create(&athread, &ta, thread_function, nullptr);
//	pthread_create(&athread, nullptr, thread_function, nullptr);
//	pthread_create(&athread, nullptr, thread_function, nullptr);

	while (1) {
		//pthread_mutex_lock(&mtx);
		printf("main waiting..\n");
		sem_wait(&bin_sem);
		printf("hi i am the main thread waiting inside critical..\n");
		scanf("%s", message);
		printf("You entered:%s\n", message);
		sem_post(&bin_sem);
		//pthread_mutex_unlock(&mtx);
	}
	sleep(5);
}
#pragma clang diagnostic pop

#pragma clang diagnostic pop