#pragma clang diagnostic push
#pragma ide diagnostic ignored "cert-msc32-c"
#pragma ide diagnostic ignored "cert-msc30-c"
//
// Created by subangkar on 11/29/18.
//

#include <stdio.h>
#include <pthread.h>
#include <semaphore.h>
#include <queue>
#include <zconf.h>
#include <string>
#include <iostream>

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
using namespace std;


#define RANDOM_TIME static_cast<unsigned int>(rand()%4)


#define MAX_NO_OF_CAKES 5

#define CAKE_CHOCOLATE "Chocolate Cake"
#define CAKE_VANILLA "Vanilla Cake"

#define CHEF_X string("Chef_X")
#define CHEF_Y string("Chef_Y")
#define CHEF_Z string("Chef_Z")

typedef string cake_t;
typedef string chef_t;

//semaphore to control sleep and wake up
sem_t emp;
sem_t full;
queue<int> q;
pthread_mutex_t lock;

pthread_mutex_t lock_console;

pthread_mutex_t lock_Q1;
pthread_mutex_t lock_Q2;
pthread_mutex_t lock_Q3;

queue<cake_t> q1;

sem_t q1_full;
sem_t q1_empty;

// P() decrements
void semaphoreP(sem_t &sem) {
	sem_wait(&sem);
}

// V() increments
void semaphoreV(sem_t &sem) {
	sem_post(&sem);
}


void init_semaphore() {
	sem_init(&emp, 0, 5);
	sem_init(&full, 0, 0);
	pthread_mutex_init(&lock, nullptr);
}

void initLocks() {
	sem_init(&q1_empty, 0, MAX_NO_OF_CAKES);
	sem_init(&q1_full, 0, 0);

	pthread_mutex_init(&lock_Q1, nullptr);
	pthread_mutex_init(&lock_Q2, nullptr);
	pthread_mutex_init(&lock_Q3, nullptr);

	pthread_mutex_init(&lock_console, nullptr);
}

void *ProducerFunc(void *arg) {
	printf("%s\n", (char *) arg);
	int i;
	for (i = 1; i <= 100; i++) {
		sem_wait(&emp);

		pthread_mutex_lock(&lock);
//		sleep(1);
		q.push(i);
		printf("producer produced item %d\n", i);

		pthread_mutex_unlock(&lock);

		sem_post(&full);
	}
}

void *ConsumerFunc(void *arg) {
	printf("%s\n", (char *) arg);
	int i;
	for (i = 1; i <= 96; i++) {
		sem_wait(&full);

		pthread_mutex_lock(&lock);

//		sleep(1);
		int item = q.front();
		q.pop();
		printf("consumer consumed item %d\n", item);

		pthread_mutex_unlock(&lock);

		sem_post(&emp);
	}
}

void printInConsole(const string &msg) {
//	pthread_mutex_lock(&lock_console);
	cout << msg << endl;
//	pthread_mutex_unlock(&lock_console);
}


void putInQ1(const chef_t &chef, const cake_t &cake) {
	pthread_mutex_lock(&lock_Q1);
	q1.push(cake);
	pthread_mutex_lock(&lock_console);
	pthread_mutex_unlock(&lock_Q1);
	printInConsole(chef + " has just put a " + cake + " in Queue1\n" + "#Cakes in Q1: " + to_string(q1.size()));
	pthread_mutex_unlock(&lock_console);
}

cake_t popFromQ1(const chef_t &chef) {
	pthread_mutex_lock(&lock_Q1);
	cake_t cake = q1.front();
	q1.pop();
	pthread_mutex_lock(&lock_console);
	pthread_mutex_unlock(&lock_Q1);
	printInConsole(chef + " has just taken a " + cake + " from Queue1\n" + "#Cakes in Q1: " + to_string(q1.size()));
	pthread_mutex_unlock(&lock_console);
	return cake;
}

void *vanillaChef(void *arg) {
	printInConsole((char *) arg);
	while (true) {
		semaphoreP(q1_empty);
		sleep(RANDOM_TIME);
		putInQ1(CHEF_Y, CAKE_VANILLA);
		semaphoreV(q1_full);
	}
}

void *chocolateChef(void *arg) {
	printInConsole((char *) arg);
	while (true) {
		semaphoreP(q1_empty);
		sleep(RANDOM_TIME);
		putInQ1(CHEF_X, CAKE_CHOCOLATE);
		semaphoreV(q1_full);
	}
}

void *carrierChef(void *arg) {
	printInConsole((char *) arg);
	while (true) {
//		sleep(4);
		sem_wait(&q1_full);
		sleep(RANDOM_TIME);
		cake_t cake = popFromQ1(CHEF_Z);
		sem_post(&q1_empty);
	}
}

int main() {

	pthread_t threadChefX, threadChefY, threadChefZ;
	srand(static_cast<unsigned int>(time(nullptr)));

//	pthread_t thread1;
//	pthread_t thread2;

//	init_semaphore();
	initLocks();

	char *message1 = "i am producer";
	char *message2 = "i am consumer";

//	pthread_create(&thread1, nullptr, ProducerFunc, (void *) message1);
//	pthread_create(&thread2, nullptr, ConsumerFunc, (void *) message2);

	pthread_create(&threadChefX, nullptr, chocolateChef, (void *) "Chef X Started");
	pthread_create(&threadChefY, nullptr, vanillaChef, (void *) "Chef Y Started");
	pthread_create(&threadChefZ, nullptr, carrierChef, (void *) "Chef Z Started");


//	pthread_join(thread1, nullptr);
//	pthread_join(thread2, nullptr);
	pthread_join(threadChefX, nullptr);
	pthread_join(threadChefY, nullptr);
	pthread_join(threadChefZ, nullptr);
//	while(true);
	return 0;
}

#pragma clang diagnostic pop
#pragma clang diagnostic pop