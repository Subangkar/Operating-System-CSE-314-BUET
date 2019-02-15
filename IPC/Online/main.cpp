#define INSERTER 1
#define SORTER 2
#define GETTER 3

#define SHM_KEY 1234
#define SEM_KEY 2345

#define CMD_SORT "Sort"
#define CMD_SORTED "Sorted"
#define CMD_TEMINATE "Terminate"

#include <iostream>
#include <sys/wait.h>
#include "SharedMem.h"
#include "SemaphoreUtils.h"
#include <algorithm>


using namespace std;


void printArr(int arr[], int n) {
	for (int i = 0; i < n; ++i) {
		cout << arr[i] << " ";
	}
	cout << endl;
}

void insert(int arr[], int &n) {
	cout << "Enter N: ";
	cin >> n;
	for (int i = 0; i < n; ++i) {
		cin >> arr[i];
	}
	printArr(arr, n);
}

void append(int arr[], int &n) {
	int t;
	cout << "Enter N(ap): ";
	cin >> t;
	for (int i = n; i < n + t; ++i) {
		cin >> arr[i];
	}
	n = n + t;
	printArr(arr, n);
}

void getFirst(shared_use_st *sharedMem) {
	int semID = semaphore_get(SEM_KEY);
	while(!sharedMem->buf_size);
	semaphore_P(semID);
	cout << getpid() << " Executing " << "getFirst()" << endl;
	if (sharedMem->buf_size)cout << sharedMem->buffer[0] << endl;
	else cout << "Buffer is Empty" << endl;
	semaphore_V(semID);
}

void sortExec(shared_use_st *sharedMem) {
	while (sharedMem->writtenBy != INSERTER && sharedMem->cmd != CMD_SORT);
	int semID = semaphore_get(SEM_KEY);
	semaphore_P(semID);
	cout << getpid() << " Executing " << "sortExec()" << endl;
	int n = sharedMem->buf_size;//sizeof(sharedMem->buffer) / sizeof(sharedMem->buffer[0]);
	sort(sharedMem->buffer, sharedMem->buffer + n);
	sharedMem->cmd = CMD_SORTED;
	sharedMem->writtenBy = SORTER;
	semaphore_V(semID);
}


void initSemaphore() {
	cout << getpid() << " Executing " << "initSemaphore()" << endl;
	int sem_id = semaphore_get(SEM_KEY);
	if (!semaphore_setValue(sem_id)) {
		cout << "Sem Problem" << endl;
		exit(EXIT_FAILURE);
	}
}

int main() {
	pid_t inserter, sorter, getter, parent;
	parent = getpid();
	cout << "Parent " << parent << endl;


	initSemaphore();

	int shmid = sharedMem_get(1234);

	inserter = fork();
	if (inserter) {
		cout << "Inserter : " << inserter << endl;
	} else {
		// INSERTER
		shared_use_st *sharedMem = (shared_use_st *) sharedMem_getAddress(shmid);
		insert(sharedMem->buffer, sharedMem->buf_size);
//		printArr(sharedMem->buffer, sharedMem->buf_size);
		while (true) {
			sharedMem->cmd = CMD_SORT;
			sharedMem->writtenBy = INSERTER;
			cout << "Here" << endl;
			while (sharedMem->writtenBy != SORTER);
			if (sharedMem->cmd == CMD_TEMINATE) {
				printArr(sharedMem->buffer, sharedMem->buf_size);
				exit(EXIT_SUCCESS);
			} else if (sharedMem->cmd == CMD_SORTED) {
				printArr(sharedMem->buffer, sharedMem->buf_size);
				append(sharedMem->buffer, sharedMem->buf_size);
			}
		}
	}
	sorter = fork();
	if (sorter) {
		cout << "Sorter : " << sorter << endl;
	} else {

		// SORTER
		shared_use_st *sharedMem = (shared_use_st *) sharedMem_getAddress(shmid);
		sortExec(sharedMem);
		exit(EXIT_SUCCESS);
	}
	getter = fork();
	if (getter) {
		cout << "Getter : " << getter << endl;
	} else {

		// GETTER
		shared_use_st *sharedMem = (shared_use_st *) sharedMem_getAddress(shmid);
		getFirst(sharedMem);
		exit(EXIT_SUCCESS);
	}



	int stat;
	while (wait(&stat) > 0);
	cout << getpid() << " Terminated" << endl;
	return 0;
}