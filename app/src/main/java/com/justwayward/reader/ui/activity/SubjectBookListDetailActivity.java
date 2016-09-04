package com.justwayward.reader.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.justwayward.reader.R;
import com.justwayward.reader.base.BaseRVActivity;
import com.justwayward.reader.base.Constant;
import com.justwayward.reader.bean.BookListDetail;
import com.justwayward.reader.component.AppComponent;
import com.justwayward.reader.component.DaggerSubjectBookListDetailActivityComponent;
import com.justwayward.reader.ui.contract.SubjectBookListDetailContract;
import com.justwayward.reader.ui.easyadapter.SubjectBookListDetailBooksAdapter;
import com.justwayward.reader.ui.presenter.SubjectBookListDetailPresenter;
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter;
import com.yuyh.easyadapter.glide.GlideCircleTransform;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 书单详情
 */
public class SubjectBookListDetailActivity extends BaseRVActivity<BookListDetail.BookListBean.BooksBean> implements SubjectBookListDetailContract.View {

    private HeaderViewHolder headerViewHolder;

    static class HeaderViewHolder {
        @Bind(R.id.tvBookListTitle)
        TextView tvBookListTitle;
        @Bind(R.id.tvBookListDesc)
        TextView tvBookListDesc;
        @Bind(R.id.ivAuthorAvatar)
        ImageView ivAuthorAvatar;
        @Bind(R.id.tvBookListAuthor)
        TextView tvBookListAuthor;
        @Bind(R.id.btnShare)
        TextView btnShare;

        public HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private List<BookListDetail.BookListBean.BooksBean> mAllBooks = new ArrayList<>();

    private int start = 0;
    private int limit = 20;

    @Inject
    SubjectBookListDetailPresenter mPresenter;

    public static final String INTENT_ID = "bookListId";

    public static void startActivity(Context context, String bookListId) {
        context.startActivity(new Intent(context, SubjectBookListDetailActivity.class)
                .putExtra(INTENT_ID, bookListId));
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_subject_book_list_detail;
    }

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerSubjectBookListDetailActivityComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this);
    }

    @Override
    public void initToolBar() {
        mCommonToolbar.setTitle(R.string.subject_book_list_detail);
        mCommonToolbar.setNavigationIcon(R.drawable.ab_back);
    }

    @Override
    public void initDatas() {

    }

    @Override
    public void configViews() {
        initAdapter(SubjectBookListDetailBooksAdapter.class, false, true);
        mRecyclerView.removeAllItemDecoration();
        mAdapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View headerView = LayoutInflater.from(mContext).inflate(R.layout.header_view_book_list_detail, parent, false);
                return headerView;
            }

            @Override
            public void onBindView(View headerView) {
                headerViewHolder = new HeaderViewHolder(headerView);
            }
        });

        mPresenter.attachView(this);
        mPresenter.getBookListDetail(getIntent().getStringExtra("bookListId"));
    }

    @Override
    public void showBookListDetail(BookListDetail data) {
        headerViewHolder.tvBookListTitle.setText(data.getBookList().getTitle());
        headerViewHolder.tvBookListDesc.setText(data.getBookList().getDesc());

        headerViewHolder.tvBookListAuthor.setText(data.getBookList().getAuthor().getNickname());


        Glide.with(mContext).load(Constant.IMG_BASE_URL + data.getBookList().getAuthor().getAvatar())
                .placeholder(R.drawable.avatar_default).transform(new GlideCircleTransform(mContext))
                .into(headerViewHolder.ivAuthorAvatar);

        List<BookListDetail.BookListBean.BooksBean> list = data.getBookList().getBooks();
        mAllBooks.clear();
        mAllBooks.addAll(list);
        mAdapter.clear();
        loadNextPage();
    }

    private void loadNextPage() {
        if (start < mAllBooks.size()) {
            if (mAllBooks.size() - start > limit) {
                mAdapter.addAll(mAllBooks.subList(start, start + limit));
            } else {
                mAdapter.addAll(mAllBooks.subList(start, mAllBooks.size()));
            }
            start += limit;
        } else {
            mAdapter.addAll(new ArrayList<BookListDetail.BookListBean.BooksBean>());
        }
    }

    @Override
    public void complete() {

    }

    @Override
    public void onItemClick(int position) {
        BookDetailActivity.startActivity(this, mAdapter.getItem(position).getBook().get_id());
    }

    @Override
    public void onRefresh() {
        mPresenter.getBookListDetail(getIntent().getStringExtra("bookListId"));
    }

    @Override
    public void onLoadMore() {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadNextPage();
            }
        }, 500);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subject, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(headerViewHolder);
    }
}
