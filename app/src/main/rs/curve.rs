#pragma version(1)
#pragma rs java_package_name(com.megatronxxx.curve.rs)
#pragma rs_fp_relaxed

int8_t *r;
int8_t *g;
int8_t *b;

uchar4 __attribute__((kernel)) apply(uchar4 in)
{
    in.r = r[in.r];
    in.g = g[in.g];
    in.b = b[in.b];
    return in;
}

