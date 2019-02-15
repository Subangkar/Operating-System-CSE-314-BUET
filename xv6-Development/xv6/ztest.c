#include "types.h"
#include "stat.h"
#include "user.h"
#include "param.h"
//#include "proc.h"

#define PGSIZE      4096

void runTests();

void test1(void){

	int testNum = 1;
	printf(2, "TEST %d:\n", testNum);

	int pagesAmount = 30;
    void* mallocs[pagesAmount];

    int test1pid = fork();
    if (test1pid == 0){

	    printf(2, "Starting to allocate %d pages..\n",pagesAmount, PGSIZE);
	    for (int i=0; i<pagesAmount; i++){
	   		mallocs[i] = malloc(PGSIZE);
		    printf(1, "Page %d allocated\n", i + 1);
	    }
//	    printf(2, "Starting to free pages..\n");
//	    for (int i=0; i<pagesAmount; i++){
//	   		free(mallocs[i]);
//	    }
		free(mallocs[2]);
	    exit();
	}

	wait();

	printf(2, "TEST %d PASSED!\n\n", testNum);
}

void runTests(){
	malloc(4*PGSIZE);
	if(fork() == 0){

		test1();

		exit();
	}

	wait();
}

void testPgCount(int x){
	printf(1, " #Pages to alloc: %d\n", x);
	char* p = malloc(x*PGSIZE);
	printf(1, " 2 alloc #Pages: %d\n", getpages());
	free(p);
	printf(1, " 2 free  #Pages: %d\n", getpages());
}

#define MAX_TOTAL_PAGES 30
#define MAX_PSYC_PAGES 15
void testContent(){
	int n=getpages();
	int nFreeRAM=MAX_PSYC_PAGES-n;
	int nFreeTOT=MAX_TOTAL_PAGES-n;
	int *p[nFreeTOT];
	for (int i = 0; i < nFreeRAM; ++i) {
		p[i]=(int*)sbrk(PGSIZE);
		p[i][0]= i;
		p[i][1]='\0';
	}
	printf(1, ">> RAM full\n\n");
	getpages();
	for (int i = nFreeRAM; i < nFreeTOT; ++i) {
		p[i]=(int*)sbrk(PGSIZE);
		p[i][0]= 100+i;
		p[i][1]='\0';
	}
	printf(1, ">> Disk full\n\n");
	getpages();
	printf(1, "Pages assigned\n\n");
	printf(1,"#Pages to access: %d\n",nFreeTOT);

//	p[0][0]=0;
//	getpages();
	for (int i = 0; i < nFreeRAM; ++i) {
		printf(1,"%d\n",p[i][0]);
	}
	getpages();
	for (int i = nFreeRAM; i < nFreeTOT; ++i) {
		printf(1,"%d\n",p[i][0]);
	}
	getpages();

	/// original more # of pg faults
//	for (int i = 0; i < MAX_TOTAL_PAGES-n; ++i) {
//		printf(1,"%s\n",p[i]);
//	}
//	getpages();
	/// reverse less # of pg faults
//	for (int i = MAX_TOTAL_PAGES-n-1; i >=0 ; --i) {
//		printf(1,"%s\n",p[i]);
//	}
//	getpages();
}

int
main(int argc, char *argv[]){
    printf(1, "Starting myMem Tests:\n");
	printf(1, " Initial #Pages: %d\n", getpages());

	testContent();
//	if (argc > 1)
//		testPgCount((argv[1][0] - '0') * 10 + (argv[1][1] - '0'));
//	else
//		runTests();

	printf(1, " Exiting #Pages: %d\n", getpages());
	exit();
}



