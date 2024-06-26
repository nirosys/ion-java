// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package com.amazon.ion.system;

import static com.amazon.ion.impl.lite._Private_LiteDomTrampoline.newLiteSystem;

import com.amazon.ion.IonCatalog;
import com.amazon.ion.IonReader;
import com.amazon.ion.IonSystem;
import com.amazon.ion.IonWriter;
import com.amazon.ion.SymbolTable;
import com.amazon.ion.impl._Private_IonBinaryWriterBuilder;
import com.amazon.ion.impl._Private_Utils;
import java.util.Objects;
import java.util.Optional;

/**
 * The builder for creating {@link IonSystem}s.
 * Most applications will only have one or two system instances;
 * see {@link IonSystem} for important constraints.
 * <p>
 * Builders may be configured once and reused to construct multiple
 * objects. They can be {@link #copy() copied} to create a mutable
 * copy of a prototype (presumably for altering some property).
 * <p>
 * <b>Instances of this class are not safe for use by multiple threads unless
 * they are {@linkplain #immutable() immutable}.</b>
 * <p>
 * The easiest way to get going is to use the {@link #standard()} builder:
 *<pre>
 *    IonSystem ion = IonSystemBuilder.standard().build();
 *</pre>
 * <p>
 * However, most long-lived applications will want to provide a custom
 * {@link IonCatalog} implementation rather than using the default
 * {@link SimpleCatalog}.  For example:
 *<pre>
 *    IonCatalog catalog = newCustomCatalog();
 *    IonSystemBuilder b = IonSystemBuilder.standard().copy();
 *    b.setCatalog(catalog);
 *    IonSystem ion = b.build();
 *</pre>
 * <p>
 * Configuration properties follow the standard JavaBeans idiom in order to be
 * friendly to dependency injection systems.  They also provide alternative
 * mutation methods that enable a more fluid style:
 *<pre>
 *    IonCatalog catalog = newCustomCatalog();
 *    IonSystem ion = IonSystemBuilder.standard()
 *                                    .withCatalog(catalog)
 *                                    .build();
 *</pre>
 *
 * <h2>Configuration Properties</h2>
 * <p>
 * This builder provides the following configurable properties:
 * <ul>
 *   <li>
 *     <b>catalog</b>: The {@link IonCatalog} used as a default when reading Ion
 *     data. If null, each system will be built with a new
 *     {@link SimpleCatalog}.
 *   </li>
 *   <li>
 *     <b>streamCopyOptimized</b>: When true, this enables optimizations when
 *     copying data between two Ion streams. For example, in some cases raw
 *     binary-encoded Ion can be copied directly from the input to the output.
 *     This can have significant performance benefits when the appropriate
 *     conditions are met. <b>This feature is experimental! Please test
 *     thoroughly and report any issues.</b>
 *   </li>
 * </ul>
 */
public class IonSystemBuilder
{
    private static final IonSystemBuilder STANDARD = new IonSystemBuilder();

    /**
     * The standard builder of {@link IonSystem}s.
     * See the class documentation for the standard configuration.
     * <p>
     * The returned instance is immutable.
     */
    public static IonSystemBuilder standard()
    {
        return STANDARD;
    }


    //=========================================================================

    IonCatalog myCatalog;
    boolean myStreamCopyOptimized = false;

    IonTextWriterBuilder textWriterBuilder = IonTextWriterBuilder.standard().withCharsetAscii();
    IonBinaryWriterBuilder binaryWriterBuilder = IonBinaryWriterBuilder.standard();
    IonReaderBuilder readerBuilder = IonReaderBuilder.standard();


    /** You no touchy. */
    private IonSystemBuilder()
    {
        // empty
    }

    private IonSystemBuilder(IonSystemBuilder that)
    {
        this.myCatalog      = that.myCatalog;
        this.myStreamCopyOptimized = that.myStreamCopyOptimized;
        this.textWriterBuilder = that.textWriterBuilder;
        this.binaryWriterBuilder = that.binaryWriterBuilder;
        this.readerBuilder = that.readerBuilder;
    }

    //=========================================================================

    /**
     * Creates a mutable copy of this builder.
     * @return a new builder with the same configuration as {@code this}.
     */
    public final IonSystemBuilder copy()
    {
        return new Mutable(this);
    }

    /**
     * Returns an immutable builder configured exactly like this one.
     *
     * @return this instance, if immutable;
     * otherwise an immutable copy of this instance.
     *
     */
    public IonSystemBuilder immutable()
    {
        return this;
    }

    /**
     * Returns a mutable builder configured exactly like this one.
     *
     * @return this instance, if mutable;
     * otherwise a mutable copy of this instance.
     *

     */
    public IonSystemBuilder mutable()
    {
        return copy();
    }

    void mutationCheck()
    {
        throw new UnsupportedOperationException("This builder is immutable");
    }


    //=========================================================================
    // Properties

    /**
     * Gets the catalog to use when building an {@link IonSystem}.
     * By default, this property is null.
     *
     * @see #setCatalog(IonCatalog)
     * @see #withCatalog(IonCatalog)
     * @see IonSystem#getCatalog()
     */
    public final IonCatalog getCatalog()
    {
        return myCatalog;
    }

    /**
     * Sets the catalog to use when building an {@link IonSystem}.
     *
     * @param catalog the catalog to use in built systems.
     *  If null, each system will be built with a new {@link SimpleCatalog}.
     *
     * @see #getCatalog()
     * @see #withCatalog(IonCatalog)
     * @see IonSystem#getCatalog()
     *
     * @throws UnsupportedOperationException if this is immutable.
     */
    public final void setCatalog(IonCatalog catalog)
    {
        mutationCheck();
        myCatalog = catalog;
    }

    /**
     * Declares the catalog to use when building an {@link IonSystem},
     * returning a new mutable builder if this is immutable.
     *
     * @param catalog the catalog to use in built systems.
     *  If null, each system will be built with a new {@link SimpleCatalog}.
     *
     * @see #getCatalog()
     * @see #setCatalog(IonCatalog)
     * @see IonSystem#getCatalog()
     */
    public final IonSystemBuilder withCatalog(IonCatalog catalog)
    {
        IonSystemBuilder b = mutable();
        b.setCatalog(catalog);
        return b;
    }


    //=========================================================================


    /**
     * Indicates whether built systems may attempt to optimize
     * {@link IonWriter#writeValue(IonReader)} by copying raw source data.
     * By default, this property is false.
     *
     * @see #setStreamCopyOptimized(boolean)
     * @see #withStreamCopyOptimized(boolean)
     *

     */
    public final boolean isStreamCopyOptimized()
    {
        return myStreamCopyOptimized;
    }

    /**
     * Declares whether built systems may attempt to optimize
     * {@link IonWriter#writeValue(IonReader)} by copying raw source data.
     * By default, this property is false.
     * <p>
     * <b>This feature is experimental! Please test thoroughly and report any
     * issues.</b>
     *
     * @throws UnsupportedOperationException if this is immutable.
     *
     * @see #isStreamCopyOptimized()
     * @see #withStreamCopyOptimized(boolean)
     *

     */
    public final void setStreamCopyOptimized(boolean optimized)
    {
        mutationCheck();
        myStreamCopyOptimized = optimized;
    }

    /**
     * Declares whether built systems may attempt to optimize
     * {@link IonWriter#writeValue(IonReader)} by copying raw source data,
     * returning a new mutable builder if this is immutable.
     * <p>
     * <b>This feature is experimental! Please test thoroughly and report any
     * issues.</b>
     *
     * @see #isStreamCopyOptimized()
     * @see #setStreamCopyOptimized(boolean)
     *

     */
    public final IonSystemBuilder withStreamCopyOptimized(boolean optimized)
    {
        IonSystemBuilder b = mutable();
        b.setStreamCopyOptimized(optimized);
        return b;
    }

    //=========================================================================

    /**
     * Gets the text writer builder whose options will be used when building an
     * {@link IonSystem}. By default, {@link IonTextWriterBuilder#standard()}
     * using {@code US-ASCII} encoding will be used.
     *
     * @see #setIonTextWriterBuilder(IonTextWriterBuilder)
     * @see #withIonTextWriterBuilder(IonTextWriterBuilder)
     */
    public final IonTextWriterBuilder getIonTextWriterBuilder() {
        return textWriterBuilder;
    }

    /**
     * Sets the text writer builder whose options will be used when building
     * an {@link IonSystem}.
     *
     * @param builder the writer builder to use in built systems.
     *  If unset, each system will be built with {@link IonTextWriterBuilder#standard()}
     *  using {@code US-ASCII} encoding.
     *
     * @see #getIonTextWriterBuilder()
     * @see #withIonTextWriterBuilder(IonTextWriterBuilder)
     *
     * @throws UnsupportedOperationException if this is immutable.
     */
    public final void setIonTextWriterBuilder(IonTextWriterBuilder builder) {
        mutationCheck();
        textWriterBuilder = Objects.requireNonNull(builder);
    }

    /**
     * Declares the text writer builder whose options will be used when building
     * an {@link IonSystem}, returning a new mutable builder if this is immutable.
     * The writer builder's catalog will never be used; the catalog provided to
     * {@link #setCatalog(IonCatalog)} or {@link #withCatalog(IonCatalog)} will
     * always be used instead.
     *
     * @param builder the writer builder to use in built systems.
     *  If unset, each system will be built with {@link IonTextWriterBuilder#standard()}
     *  using {@code US-ASCII} encoding.
     *
     * @see #getIonTextWriterBuilder()
     * @see #setIonTextWriterBuilder(IonTextWriterBuilder)
     */
    public final IonSystemBuilder withIonTextWriterBuilder(IonTextWriterBuilder builder)
    {
        IonSystemBuilder b = mutable();
        b.setIonTextWriterBuilder(builder);
        return b;
    }

    //=========================================================================

    /**
     * Gets the binary writer builder whose options will be used when building an
     * {@link IonSystem}. By default, {@link IonBinaryWriterBuilder#standard()} will
     * be used.
     *
     * @see #setIonBinaryWriterBuilder(IonBinaryWriterBuilder)
     * @see #withIonBinaryWriterBuilder(IonBinaryWriterBuilder)
     */
    public final IonBinaryWriterBuilder getIonBinaryWriterBuilder() {
        return binaryWriterBuilder;
    }

    /**
     * Sets the binary writer builder whose options will be used when building
     * an {@link IonSystem}. The writer builder's catalog will never be used; the
     * catalog provided to {@link #setCatalog(IonCatalog)} or
     * {@link #withCatalog(IonCatalog)} will always be used instead.
     *
     * @param builder the writer builder to use in built systems.
     *  If unset, each system will be built with {@link IonBinaryWriterBuilder#standard()}.
     *
     * @see #getIonBinaryWriterBuilder()
     * @see #withIonBinaryWriterBuilder(IonBinaryWriterBuilder)
     *
     * @throws UnsupportedOperationException if this is immutable.
     */
    public final void setIonBinaryWriterBuilder(IonBinaryWriterBuilder builder) {
        mutationCheck();
        binaryWriterBuilder = Objects.requireNonNull(builder);
    }

    /**
     * Declares the binary writer builder whose options will be used to use when building
     * an {@link IonSystem}, returning a new mutable builder if this is immutable.
     * The writer builder's catalog will never be used; the catalog provided to
     * {@link #setCatalog(IonCatalog)} or {@link #withCatalog(IonCatalog)} will
     * always be used instead.
     *
     * @param builder the writer builder to use in built systems.
     *  If unset, each system will be built with {@link IonBinaryWriterBuilder#standard()}.
     *
     * @see #getIonBinaryWriterBuilder()
     * @see #setIonBinaryWriterBuilder(IonBinaryWriterBuilder)
     */
    public final IonSystemBuilder withIonBinaryWriterBuilder(IonBinaryWriterBuilder builder)
    {
        IonSystemBuilder b = mutable();
        b.setIonBinaryWriterBuilder(builder);
        return b;
    }

    //=========================================================================

    /**
     * Gets the reader builder whose options will be used when building an
     * {@link IonSystem}. By default, {@link IonReaderBuilder#standard()} will
     * be used.
     *
     * @see #setReaderBuilder(IonReaderBuilder)
     * @see #withReaderBuilder(IonReaderBuilder)
     */
    public final IonReaderBuilder getReaderBuilder() {
        return readerBuilder;
    }

    /**
     * Sets the reader builder whose options will be used to use when building
     * an {@link IonSystem}. The reader builder's catalog will never be used; the
     * catalog provided to {@link #setCatalog(IonCatalog)} or
     * {@link #withCatalog(IonCatalog)} will always be used instead.
     *
     * @param builder the reader builder to use in built systems.
     *  If unset, each system will be built with {@link IonReaderBuilder#standard()}.
     *
     * @see #getReaderBuilder()
     * @see #withReaderBuilder(IonReaderBuilder)
     *
     * @throws UnsupportedOperationException if this is immutable.
     */
    public final void setReaderBuilder(IonReaderBuilder builder) {
        mutationCheck();
        readerBuilder = Objects.requireNonNull(builder);
    }

    /**
     * Declares the reader builder whose options will be used to use when building
     * an {@link IonSystem}, returning a new mutable builder if this is immutable.
     * The reader builder's catalog will never be used; the catalog provided to
     * {@link #setCatalog(IonCatalog)} or {@link #withCatalog(IonCatalog)} will
     * always be used instead.
     *
     * @param builder the reader builder to use in built systems.
     *  If unset, each system will be built with {@link IonReaderBuilder#standard()}.
     *
     * @see #getReaderBuilder()
     * @see #setReaderBuilder(IonReaderBuilder)
     */
    public final IonSystemBuilder withReaderBuilder(IonReaderBuilder builder)
    {
        IonSystemBuilder b = mutable();
        b.setReaderBuilder(builder);
        return b;
    }

    //=========================================================================

    /**
     * Builds a new {@link IonSystem} instance based on this builder's
     * configuration properties.
     */
    public final IonSystem build()
    {
        IonCatalog catalog = Optional.ofNullable(myCatalog).orElseGet(SimpleCatalog::new);

        IonTextWriterBuilder twb = textWriterBuilder.copy().withCatalog(catalog);
        IonBinaryWriterBuilder bwb = binaryWriterBuilder.copy().withCatalog(catalog);
        IonReaderBuilder rb = readerBuilder.copy().withCatalog(catalog);

        // TODO Use #setStreamCopyOptimized directly on the BWB
        bwb.setStreamCopyOptimized(myStreamCopyOptimized);

        // TODO Would be nice to remove this since it's implied by the BWB.
        //      However that currently causes problems in the IonSystem
        //      constructors (which get a null initialSymtab).
        SymbolTable systemSymtab = _Private_Utils.systemSymtab(1);
        bwb.setInitialSymbolTable(systemSymtab);
        // This is what we need, more or less.
        //     bwb = bwb.fillDefaults();

        return newLiteSystem(twb, (_Private_IonBinaryWriterBuilder) bwb, rb);
    }

    //=========================================================================

    private static final class Mutable
        extends IonSystemBuilder
    {
        private Mutable(IonSystemBuilder that)
        {
            super(that);
        }

        @Override
        public IonSystemBuilder immutable()
        {
            return new IonSystemBuilder(this);
        }

        @Override
        public IonSystemBuilder mutable()
        {
            return this;
        }

        @Override
        void mutationCheck()
        {
        }
    }
}
