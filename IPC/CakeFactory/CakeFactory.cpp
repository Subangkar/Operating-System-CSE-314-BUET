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

bool timeoutPassed = false; // debug


#define MAX_NO_OF_CAKES 5
#define MAX_NO_OF_PRODUCDED_CAKES 5
#define MAX_NO_OF_VANILLA_CAKES 5
#define MAX_NO_OF_CHOCOLATE_CAKES 5

#define CAKE_CHOCOLATE "Chocolate Cake"
#define CAKE_VANILLA "Vanilla Cake"

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


// V(Q1) here
void putInQ1(const chef_t &chef, const cake_t &cake) {
	lock(lock_Q1);
	q1.push(cake);
	unsigned long qSize = q1.size();
	semaphoreV(q1_full);

	lock(lock_console);
	unlock(lock_Q1);
	printInConsole(chef + " has just put a " + cake + " in Queue1\n" + "#Cakes in Q1: " + to_string(qSize));
	unlock(lock_console);
}

// V(Q1) here
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
//	size_t qSize = q1.size();
	unlock(lock_Q1);

	printInConsoleWithLocking(chef + " has checked and found " + cake + " at top of Queue1\n");
	// + "#Cakes in Q1: " +
	//			to_string(qSize)
	return cake;
}


void *vanillaChef(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (!timeoutPassed) {
		printInConsoleWithLocking(">> " + CHEF_Y + " is waiting to put a " + CAKE_VANILLA + " in Q1.....");
		semaphoreP(q1_empty);
		printInConsoleWithLocking(">> " + CHEF_Y + " has started supplying " + CAKE_VANILLA + " in Q1.");

//		sleep(RANDOM_TIME);
		putInQ1(CHEF_Y, CAKE_VANILLA);
	}
}

void *chocolateChef(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (!timeoutPassed) {
		printInConsoleWithLocking(">> " + CHEF_X + " is waiting to put a " + CAKE_CHOCOLATE + " in Q1.....");
		semaphoreP(q1_empty);
		printInConsoleWithLocking(">> " + CHEF_X + " has started supplying " + CAKE_CHOCOLATE + " in Q1.");

//		sleep(RANDOM_TIME);
		putInQ1(CHEF_X, CAKE_CHOCOLATE);
	}
}


void *carrierChef(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (!timeoutPassed) {
		printInConsoleWithLocking(">> " + CHEF_Z + " is waiting to take a Cake From Q1.....");
		semaphoreP(q1_full);
		printInConsoleWithLocking(">> " + CHEF_Z + " has started looking into Q1.");
//		sleep(RANDOM_TIME);
		cake_t topCakeQ1 = lookIntoQ1(CHEF_Z);
		if (topCakeQ1 == CAKE_VANILLA) {
			printInConsoleWithLocking(">> " + CHEF_Z + " is waiting to put a " + topCakeQ1 + " in Queue_Vanilla.....");
			semaphoreP(qVanilla_empty);
			printInConsoleWithLocking(">> " + CHEF_Z + " has started supplying " + topCakeQ1 + " in Queue_Vanilla.");

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
			printInConsoleWithLocking(
					">> " + CHEF_Z + " is waiting to put a " + topCakeQ1 + " in Queue_Chocolate.....");
			semaphoreP(qChocolate_empty);
			printInConsoleWithLocking(">> " + CHEF_Z + " has started supplying " + topCakeQ1 + " in Queue_Chocolate.");

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
	while (!timeoutPassed) {
		printInConsoleWithLocking(">> " + WAITER_VANILLA + " is waiting to take " + CAKE_VANILLA + ".....");
		semaphoreP(qVanilla_full);
		printInConsoleWithLocking(">> " + WAITER_VANILLA + " has started selling " + " from Queue_Vanilla.");

//		sleep(RANDOM_TIME);

		lock(lock_VanillaQ);
		size_t qSize = --nVanilla;
		semaphoreV(qVanilla_empty);//now waiting one will start & reach upto lock

		lock(lock_console);
		unlock(lock_VanillaQ);
		printInConsole(WAITER_VANILLA + " has just sold a cake from Queue_Vanilla\n" + "#Cakes in QVanilla: " +
		               to_string(qSize));
		unlock(lock_console);
	}
}

void *chocolateWaiter(void *arg) {
	printInConsoleWithLocking((char *) arg);
	while (!timeoutPassed) {
		printInConsoleWithLocking(">> " + WAITER_CHOCOLATE + " is waiting to take " + CAKE_CHOCOLATE + ".....");
		semaphoreP(qChocolate_full);
		printInConsoleWithLocking(">> " + WAITER_CHOCOLATE + " has started selling " + " from Queue_Chocolate.");

//		sleep(RANDOM_TIME);

		lock(lock_ChocolateQ);
		size_t qSize = --nChocolate;
		semaphoreV(qChocolate_empty);

		lock(lock_console);
		unlock(lock_ChocolateQ);// newer one can write to q now but can't print that before this
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

//	sleep(1);
	pthread_create(&threadChefX, nullptr, chocolateChef, (void *) "\n>>> Chef X Started\n");
	pthread_create(&threadChefY, nullptr, vanillaChef, (void *) "\n>>> Chef Y Started\n");
	pthread_create(&threadChefZ, nullptr, carrierChef, (void *) "\n>>> Chef Z Started\n");

	sleep(1);
	pthread_create(&threadWaiterChocolate, nullptr, chocolateWaiter, (void *) "\n>>> Chocolate Waiter Started\n");
	pthread_create(&threadWaiterVanilla, nullptr, vanillaWaiter, (void *) "\n>>> Vanilla Waiter Started\n");

	sleep(1);
	timeoutPassed = true;

//	pthread_join(threadChefX, nullptr);
//	pthread_join(threadChefY, nullptr);
//	pthread_join(threadChefZ, nullptr);
//	pthread_join(threadWaiterChocolate, nullptr);
//	pthread_join(threadWaiterVanilla, nullptr);

	return 0;
}

#pragma clang diagnostic pop
#pragma clang diagnostic pop