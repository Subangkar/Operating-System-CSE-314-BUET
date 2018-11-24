#include <iostream>
#include <sys/wait.h>
#include "SemaphoreUtils.h"


#define NO_CHILD_PROCESSES 3
#define NO_PROCESSES 3 // k -> 2^k

using namespace std;


void criticSec() {
	static int counter = 0;
	counter++;
	for (int i = 0; i < 2; i++) {
		cout << counter << " -> " << getpid() << " : " << i << endl;
//		printf("%d hello", getpid());
//		fflush(stdout);
		sleep(1);
//		printf("world\n");
		fflush(stdout);
	}
}

int main() {
	cout << "Source   PID: " << getpid() << endl;
	const int initPID = getpid();
	int sem_id = semaphore_create(4200);
	cout << "Semaphore ID: " << sem_id << endl;

	if (!semaphore_setValue(sem_id, 0, 1))exit(EXIT_FAILURE);

	pid_t pid = -1;

	for (int i = 0; i < NO_PROCESSES; ++i) fork();

//	for (int i = 0;pid && i < NO_CHILD_PROCESSES; ++i)pid = fork();


	if (!semaphore_P(sem_id)) exit(EXIT_FAILURE);
	criticSec();
	if (!semaphore_V(sem_id)) exit(EXIT_FAILURE);

	int stat;
	while (wait(&stat) > 0);
	if (getpid() == initPID && !semaphore_delete(sem_id)) exit(EXIT_FAILURE);

	return 0;
}