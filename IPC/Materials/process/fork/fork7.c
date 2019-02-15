#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>

int main( int argc,char* argv[]) {
	char *argv1[4] = {"Command-line", "hello", "bla.txt",NULL};

	int pid = fork();

	if ( pid == 0 ) {
		execvp( "grep", argv1 );
	}

	/* Put the parent to sleep for 10 seconds--let the child finished executing */
	// sleep(10);
	wait(&pid);
	printf( "Finished executing the parent process\n"
	        " - the child won't get here--you will only see this once\n" );

	return 0;
}
