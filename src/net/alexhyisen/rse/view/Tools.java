package net.alexhyisen.rse.view;

import java.util.stream.IntStream;

/**
 * Created by Alex on 2016/11/27.
 * some useful tools.
 */
class Tools {static IntStream revRange(int from,int to){
        return IntStream.range(to,from).map(i->from-1-(i-to));
    }
}
