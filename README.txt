UTEID: kdh2289; jhm2464;
FIRSTNAME: Kaelin; Jeremiah;
LASTNAME: Hooper; Martinez;
CSACCOUNT: kaelin; jeremiah;
EMAIL: kaelin@cs.utexas.edu; Jeremiah.H.Martinez@utexas.edu;

[Program 1]
[Description]

[Finish]
Finished it. Works for .bmp and .png files.

[Answer of Questions]
[Question 1]
Comparing your original and modified images carefully, can you detect *any* difference visually (that is, in the appearance of the image)?

[Answer 1]
Nope! Not that we can see.

[Question 2]
Can you think of other ways you might hide the message in image files (or in other types of files)?

[Answer 2]
Maybe same method, just store the inverse bit to throw people off.
And start at the bottom right corner and work your way to the left and up

[Question 3]
Can you invent ways to increase the bandwidth of the channel?

[Answer 3]
Use the last two bits of RBG instead of just one.

[Question 4]
Suppose you were tasked to build an "image firewall" that would block images containing hidden messages. Could you do it? How might you approach this problem?

[Answer 4]
Maybe change all the last bits of all RGBs to 1 or 0. Maybe last two to be safe.

[Question 5]
Does this fit our definition of a covert channel? Explain your answer.
Yes. The pixels are being used in ways they were not intended to be used for.
We should not be able to retrieve ascii messages from a picture.

[Answer 5]
