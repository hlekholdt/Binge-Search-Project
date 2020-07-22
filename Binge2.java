package prog12;

import java.util.List;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import prog06.ArrayQueue;

public class Binge2 implements SearchEngine {
    
	HardDisk<PageFile> pageDisk = new HardDisk<PageFile>();
	PageTrie URLToIndex = new PageTrie();
	int refCount;
	
	//Put a HardDisk variable wordDisk into Binge ... what is T???
	HardDisk<List<Long>> wordDisk = new HardDisk<List<Long>>();
	//Create a wordToIndex table which maps a word to its index
	WordTable wordToIndex = new WordTable();
	
	
    public void gather (Browser browser, List<String> startingURLs) {
    	URLToIndex.read(pageDisk);
    	wordToIndex.read(wordDisk);
    }
    
    
    /* Search for up to numResults pages containing all keyWords and
     * return them in an array in order of decreasing importance
     * (number of references). */
    public String[] search (List<String> keyWords, int numResults) {
    	/*we're going to use an iterator of type Long because each word has an array of page indices
    	 *and we want an iterator that runs through them */
    	Iterator<Long>[] wordFileIterators = (Iterator<Long>[]) new Iterator[keyWords.size()];
    	
    	long[] currentPageIndices = new long[keyWords.size()];

    	
    	//Every file on the disk has a list of words that were listed on the webpage
    	//Victor (and every word on the web page) has a file with page indices! 
    	PriorityQueue<Long> bestPageIndices = new PriorityQueue<Long>(numResults, new PageComparator());
    	
    	for(int i = 0; i < keyWords.size(); i++) {
    		//look at wordToIndex and see if it contains wordIndex & if not we have to return an emptry array of string
    		//once you get the index --> go to the wordDisk and get the iterator from there
    		//"Java" --> index --> wordDisk (here we can grab iterator)
    		
    		if(!wordToIndex.containsKey(keyWords.get(i))) {
    			return new String[0];
    		}
    		else {
    			long wordIndex = wordToIndex.get(keyWords.get(i));
    			wordFileIterators[i] = wordDisk.get(wordIndex).iterator();
    		}
    	}
    	
    	//object to compare
    	PageComparator pageComparator = new PageComparator();
    	
    	while (getNextPageIndices(currentPageIndices, wordFileIterators)) {
    		//if there's a match! yay!
    		if(allEqual(currentPageIndices)) {
    			//check if the priority queue's size is equal to numResults
    			
    			Long index = currentPageIndices[0];
    			
    			if (bestPageIndices.size() < numResults || pageComparator.compare(bestPageIndices.peek(),index) < 0) {
    				
    				if(bestPageIndices.size() == numResults) {
    					//if its full we take off the top one
        				bestPageIndices.poll();
        			}
    				
    				bestPageIndices.offer(index);
    			}
    		}
    	}
    	
    	//create an array of String which will hold the results
    	String[] results = new String[bestPageIndices.size()];
    	/*unload the priority queue into the string 
    	 *(polling gives out pages in reverse order) --> backwards for loop */
    	for (int i = results.length - 1; i >= 0; i--) {
    		results[i] = pageDisk.get(bestPageIndices.poll()).url;
    	}
    	
    	return results;
    	
//    	delete this!!!
//    	return new String[0];
    	
    }
    
    /*Compare page indices to get a page file (which have ref counts) then compare ref counts to get references
     *cs.miami.edu(1111) --> ref = 9999 VS sluethacadamy(7777) --> ref = 5555 === cs.miami.edu is more significant and wins!
     *so we are doing ref count of a - ref count of b */
    public class PageComparator implements Comparator<Long>{
    	public int compare(Long a, Long b) {
    		return pageDisk.get(a).getRefCount() - pageDisk.get(b).getRefCount();
    	}
    }
    
    /** If all the currentPageIndices are the same (because are just
    	starting or just found a match), get the next page index for
    	each word: call next() for each word file iterator and put the
    	result into current page indices.

    	If they are not all the same, only get the next index if the
    	current index is smaller than the largest.

    	Return false if hasNext() is false for any iterator.

    	@param currentPageIndices array of current page indices
    	@param wordFileIterators array of iterators with next page indices
    	@return true if all minimum page indices updates, false otherwise
     */
    public boolean getNextPageIndices(long[] currentPageIndices, Iterator<Long>[] wordFileIterators) {
    	//get the largest index in currentPageIndices
    	long largestIndex = currentPageIndices[0];
    	//check to see if all indices in currentPageIndices are equal/largest
    	if(allEqual(currentPageIndices) == true) {
			for(int i = 0; i < wordFileIterators.length; i++) {
				if(wordFileIterators[i].hasNext()) {
					currentPageIndices[i] = wordFileIterators[i].next();
				}
				else
					return false;
				//currentPageIndices[i] = wordFileIterators[i].next();
			}
		}
		else {
			for(Long ind : currentPageIndices) {
				if(ind > largestIndex)
					largestIndex = ind;
			}

			for(int i = 0; i < currentPageIndices.length; i++) {
				if(currentPageIndices[i] < largestIndex) {
					
					if(wordFileIterators[i].hasNext()) {
						currentPageIndices[i] = wordFileIterators[i].next();
					}
					else
						return false;
				}	
			}	
			return true;
		}
		return true;
    }
    
    /** Check if all elements in an array are equal.
    	@param array an array of numbers
    	@return true if all are equal, false otherwise
     */
    public boolean allEqual(long[] array) {
    	for(int i = 0; i < array.length; i++) {
    		if(array[0] != array[i]) {
    			return false;
    		}
    	}
    	return true;
    }
    
}