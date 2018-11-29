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


#define RANDOM_TIME 0//// static_cast<unsigned int>(rand()%4)


#define MAX_NO_OF_CAKES 5
#define MAX_NO_OF_PRODUCDED_CAKES 5
#define MAX_NO_OF_VANILLA_CAKES 5
#define MAX_NO_OF_CHOCOLATE_CAKES 5

#define CAKE_CHOCOLATE "Chocolate Cake"
#define CAKE_VANILLA "Vanilla Cake"
#define CAKE_NULL "NULL"

#define CHEF_X string("Chef_X")
#define CHEF_Y string("Chef_Y")
#define CHEF_Z string("Chef_Z")

#define WAITER_VANILLA string("Vanilla Waiter")
#define WAITER_CHOCOLATE string("Chocolate Waiter")

typedef string cake_t;
typedef string chef_t;


pthread_mutex_t lock_console;

pthread_mutex_t lock_Q1;
pthread_mutex_t lock_VanillaQ;
pthread_mutex_t lock_ChocolateQ;

size_t nChocolate, nVanilla;

queue<cake_t> q1;

sem_t q1_full;
sem_t q1_empty;

sem_t qVanilla_full;
sem_t qVanilla_empty;

sem_t qChocolate_full;
sem_t qChocolate_empty;

// P() decrements
void semaphoreP(sem_t &sem) {
	sem_wait(&sem);
}

// V() increments
void semaphoreV(sem_t &sem) {
	sem_post(&sem);
}

void lock(pthread_mutex_t &lockVar) {
	pthread_mutex_lock(&lockVar);
}

void unlock(pthread_mutex_t &lockVar) {
	pthread_mutex_unlock(&lockVar);
}


void initLocks() {
	sem_init(&q1_empty, 0, MAX_NO_OF_PRODUCDED_CAKES);
	sem_init(&q1_full, 0, 0);

	sem_init(&qVanilla_empty, 0, MAX_NO_OF_VANILLA_CAKES);
	sem_init(&qVanilla_full, 0, 0);

	sem_init(&qChocolate_empty, 0, MAX_NO_OF_CHOCOLATE_CAKES);
	sem_init(&qChocolate_full, 0, 0);

	pthread_mutex_init(&lock_Q1, nullptr);
	pthread_mutex_init(&lock_VanillaQ, nullptr);
	pthread_mutex_init(&lock_ChocolateQ, nullptr);

	pthread_mutex_init(&lock_console, nullptr);
}

void printInConsoleWithLocking(const string &msg) {
	lock(lock_console);
	cout << msg << endl << endl;
	unlock(lock_console);
}

void printInConsole(const string &msg) {
//	pthread_mutex_lock(&lock_console);
	cout << msg << endl << endl;
//	pthread_mutex_unlock(&lock_console);
}


void putInQ1(const chef_t &chef, const cake_t &cake) {
	lock(lock_Q1);
	q1.push(cake);
	lock(lock_console);
	unlock(lock_Q1);
	printInConsole(chef + " has just put a " + cake + " in Queue1\n" + "#Cakes in Q1: " + to_string(q1.size()));
	unlock(lock_console);
}

cake_t popFromQ1(const chef_t &chef) {
	lock(lock_Q1);
	cake_t cake = q1.front();
	q1.pop();
	size_t qSize = q1.size();
	semaphoreV(q1_empty);// wake waiting chef
	lock(lock_console);
	unlock(lock_Q1);
	printInConsole(chef + " has just taken a " + cake + " from Queue1\n" + "#Cakes in Q1: " + to_string(qSize));
	unlock(lock_console);
	return cake;
}

cake_t lookIntoQ1(const chef_t &chef) {
	lock(lock_Q1);
	cake_t cake = q1.front();
	lock(lock_console);
	unlock(lock_Q1);
	printInConsole(
			chef + " has checked and found " + cake + " at top of Queue1\n" + "#Cakes in Q1: " +
			to_string(q1.size()));
	unlock(lock_console);
	return cake;
}

void *vanillaChef(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (true) {
		semaphoreP(q1_empty);
		sleep(RANDOM_TIME);
		putInQ1(CHEF_Y, CAKE_VANILLA);
		semaphoreV(q1_full);
	}
}

void *chocolateChef(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (true) {
		semaphoreP(q1_empty);
		sleep(RANDOM_TIME);
		putInQ1(CHEF_X, CAKE_CHOCOLATE);
		semaphoreV(q1_full);
	}
}


void *carrierChef(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (true) {
		semaphoreP(q1_full);
		sleep(RANDOM_TIME);
		cake_t topCakeQ1 = lookIntoQ1(CHEF_Z);
		if (topCakeQ1 == CAKE_VANILLA) {
			semaphoreP(qVanilla_empty);
			cake_t cake = popFromQ1(CHEF_Z);
			lock(lock_VanillaQ);
			size_t qSize = ++nVanilla;
			unlock(lock_VanillaQ);
			lock(lock_console);
			semaphoreV(qVanilla_full);
			printInConsole(CHEF_Z + " has just put a " + cake + " in Queue_Vanilla\n" + "#Cakes in QVanilla: " +
			               to_string(qSize));
			unlock(lock_console);
		} else if (topCakeQ1 == CAKE_CHOCOLATE) {
			semaphoreP(qChocolate_empty);
			cake_t cake = popFromQ1(CHEF_Z);
			lock(lock_ChocolateQ);
			size_t qSize = ++nChocolate;
			unlock(lock_ChocolateQ);
			lock(lock_console);
			semaphoreV(qChocolate_full);
			printInConsole(CHEF_Z + " has just put a " + cake + " in Queue_Chocolate\n" + "#Cakes in QChocolate: " +
			               to_string(qSize));
			unlock(lock_console);
		}
	}
}


void *vanillaWaiter(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (true) {
		semaphoreP(qVanilla_full);
		sleep(RANDOM_TIME);
		lock(lock_VanillaQ);
		size_t qSize = --nVanilla;
		unlock(lock_VanillaQ);
		lock(lock_console);
		semaphoreV(qVanilla_empty);
		printInConsole(WAITER_VANILLA + " has just sold a cake from Queue_Vanilla\n" + "#Cakes in QVanilla: " +
		               to_string(qSize));
		unlock(lock_console);
	}
}

void *chocolateWaiter(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (true) {
		semaphoreP(qChocolate_full);
		sleep(RANDOM_TIME);
		lock(lock_ChocolateQ);
		size_t qSize = --nChocolate;
		unlock(lock_ChocolateQ);
		lock(lock_console);
		semaphoreV(qChocolate_empty);
		printInConsole(WAITER_CHOCOLATE + " has just sold a cake from Queue_Chocolate\n" + "#Cakes in QChocolate: " +
		               to_string(qSize));
		unlock(lock_console);
	}
}


int main() {
	freopen("out.log", "w", stdout);

	srand(static_cast<unsigned int>(time(nullptr)));

	pthread_t threadChefX, threadChefY, threadChefZ;
	pthread_t threadWaiterChocolate, threadWaiterVanilla;

	initLocks();

	pthread_create(&threadChefX, nullptr, chocolateChef, (void *) "Chef X Started");
	pthread_create(&threadChefY, nullptr, vanillaChef, (void *) "Chef Y Started");
	pthread_create(&threadChefZ, nullptr, carrierChef, (void *) "Chef Z Started");

	sleep(15);
	pthread_create(&threadWaiterChocolate, nullptr, chocolateWaiter, (void *) "Chocolate Waiter Started");
	pthread_create(&threadWaiterVanilla, nullptr, vanillaWaiter, (void *) "Vanilla Waiter Started");


	pthread_join(threadChefX, nullptr);
	pthread_join(threadChefY, nullptr);
	pthread_join(threadChefZ, nullptr);

	return 0;
}

#pragma clang diagnostic pop
#pragma clang diagnostic pop