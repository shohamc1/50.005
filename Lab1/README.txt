50005Lab1
=========

Implementation Details
----------------------

Process Termination ->  The main program enters a loop and checks every process' pid. If the
                    process is not alive (pid == 0), then the task_status is checked.
                    If the task is of type 't' or 'w' (status code = 0), then the child
                    is revived using fork() and the semaphores and job buffers are updated.
                    If type is 'i' or 'z', nothing is done as the child will terminate on its own.

Revival Process -> The method used here is naive. The loop checks the exit status of each child
                process. If the child process has exited with SIGKILL code (int: 9), then it is
                restarted using fork() and the job buffer and semaphore is updated.