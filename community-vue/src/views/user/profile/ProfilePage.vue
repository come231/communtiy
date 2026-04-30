<template>
  <!-- 必须单根：UserLayout 对 router-view 使用 transition mode=out-in，多根 Fragment（主 div + 多个 el-dialog）会导致进出动画异常，整页卡在 opacity:0 呈黑屏 -->
  <div class="profile-page-root">
  <!-- 与根目录「我的主页.html」一致：浅紫渐变底 + 通栏封面 + 上浮资料卡 + 圆角标签导航 + 卡片列表 -->
  <div class="min-h-screen bg-gradient-to-br from-[#f0f4ff] via-[#f8faff] to-[#f0f4ff] text-[#1f2937]">
    <!-- 顶部封面（使用成对闭合标签，避免部分环境下 HMR 对 void div 解析异常） -->
    <div
      class="w-full h-[240px] md:h-[240px] bg-gradient-to-r from-[#6366f1] via-[#a855f7] to-[#ec4899] relative shadow-[0_8px_30px_rgba(99,102,241,0.2)]"
    ></div>

    <div class="max-w-[1000px] mx-auto px-5 relative z-10 pb-10" style="margin-top: -5rem">
      <!-- 个人信息卡片（html: profile-card mt -80px） -->
      <div
        class="bg-white rounded-[24px] p-6 md:p-8 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)]"
      >
        <div
          class="flex flex-col sm:flex-row flex-wrap items-center justify-center text-center sm:text-left gap-6 md:gap-8 mb-6"
        >
          <img
            :src="resolveFileUrl(userInfo.avatar) || defaultAvatar"
            referrerpolicy="no-referrer"
            class="w-[140px] h-[140px] rounded-full object-cover border-[6px] border-white shadow-[0_0_0_4px_#818cf8] transition-transform duration-300 hover:scale-105 bg-white shrink-0"
            @error="onAvatarImgError"
          />

          <div class="flex-1 min-w-0 w-full sm:w-auto">
            <h1
              class="text-[28px] md:text-[32px] font-bold bg-gradient-to-r from-[#6366f1] to-[#a855f7] bg-clip-text text-transparent mb-2 flex flex-wrap items-center justify-center sm:justify-start gap-2"
            >
              <span>{{ userInfo.nickname || userInfo.username }}</span>
              <span class="text-sm px-3 py-0.5 bg-[#f0f4ff] text-[#6366f1] rounded-full font-semibold">Lv.{{ level }}</span>
            </h1>
            <p class="text-[16px] text-[#6b7280] mb-4 leading-relaxed">
              {{ userInfo.bio || '这个人很懒，什么都没写~' }}
            </p>
            <div v-if="parsedBioTags.length > 0" class="flex flex-wrap gap-2.5 justify-center sm:justify-start">
              <span
                v-for="(tag, idx) in parsedBioTags"
                :key="idx"
                class="px-3.5 py-1.5 bg-[#f0f4ff] text-[#6366f1] rounded-[20px] text-[13px] font-semibold"
              >
                {{ tag }}
              </span>
            </div>
          </div>
        </div>

        <!-- 统计数字 -->
        <div class="flex justify-center flex-wrap gap-8 md:gap-10 my-2 md:my-5">
            <div class="text-center cursor-pointer transition-all duration-300 hover:-translate-y-1" @click="focusMyPosts">
              <div class="text-[26px] font-bold text-[#6366f1]">{{ userInfo.postsCount || 0 }}</div>
              <div class="text-[14px] text-[#6b7280] mt-1">帖子</div>
            </div>
            <div class="text-center cursor-pointer transition-all duration-300 hover:-translate-y-1" @click="loadFollowing">
              <div class="text-[26px] font-bold text-[#6366f1]">{{ userInfo.followingCount || 0 }}</div>
              <div class="text-[14px] text-[#6b7280] mt-1">关注</div>
            </div>
            <div class="text-center cursor-pointer transition-all duration-300 hover:-translate-y-1" @click="loadFollowers">
              <div class="text-[26px] font-bold text-[#6366f1]">{{ userInfo.followersCount || 0 }}</div>
              <div class="text-[14px] text-[#6b7280] mt-1">粉丝</div>
            </div>
            <div class="text-center">
              <div class="text-[26px] font-bold text-[#ec4899]">{{ userInfo.likeCount || 0 }}</div>
              <div class="text-[14px] text-[#6b7280] mt-1">获赞</div>
            </div>
            <div class="text-center">
              <div class="text-[26px] font-bold text-[#3b82f6]">{{ userInfo.points || 0 }}</div>
              <div class="text-[14px] text-[#6b7280] mt-1">积分</div>
            </div>
        </div>

        <!-- 操作按钮（html: action-btns） -->
        <div class="flex flex-wrap gap-3 md:gap-4 justify-center mt-4">
          <button
            type="button"
            class="px-7 py-3 rounded-[14px] font-semibold text-[15px] bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white transition-all duration-300 hover:scale-105 hover:shadow-[0_8px_20px_rgba(99,102,241,0.3)] inline-flex items-center gap-2"
            @click="showEditDialog"
          >
            <i class="fas fa-edit"></i> 编辑资料
          </button>
          <button
            type="button"
            class="px-7 py-3 rounded-[14px] font-semibold text-[15px] bg-transparent border-2 border-[#6366f1] text-[#6366f1] transition-all duration-300 hover:bg-[#6366f1] hover:text-white inline-flex items-center gap-2"
            @click="showCreateSectionDialog"
          >
            <i class="fas fa-plus"></i> 创建板块
          </button>
        </div>
      </div>

      <!-- 签到卡片 -->
      <div class="mt-6 bg-gradient-to-r from-[#f97316] via-[#ec4899] to-[#a855f7] rounded-2xl p-6 text-white relative overflow-hidden shadow-lg">
        <div class="absolute right-0 top-0 w-32 h-32 bg-white/10 rounded-full -mr-10 -mt-10"></div>
        <div class="absolute left-0 bottom-0 w-24 h-24 bg-white/10 rounded-full -ml-8 -mb-8"></div>
        <div class="relative z-10 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold flex items-center gap-2 mb-1">
              <i class="fas fa-calendar-check"></i> 每日签到
            </h3>
            <p class="text-sm text-white/80">
              已连续签到 <strong class="text-yellow-300 text-base">{{ checkinStatus.continuousDays }}</strong> 天
              · 累计 <strong class="text-yellow-300">{{ checkinStatus.totalDays }}</strong> 天
            </p>
          </div>
          <button @click="doCheckin" :disabled="checkinStatus.checkedToday || checkining"
            :class="[
              'px-8 py-3 rounded-xl font-semibold text-sm transition-all',
              checkinStatus.checkedToday
                ? 'bg-white/20 text-white/70 cursor-default'
                : 'bg-white text-[#ec4899] hover:shadow-lg hover:-translate-y-0.5'
            ]">
            {{ checkinStatus.checkedToday ? '今日已签 ✓' : (checkining ? '签到中...' : '立即签到 +5积分') }}
          </button>
        </div>
      </div>

      <!-- 导航标签（html: nav-tabs / tab） -->
      <div class="flex gap-2.5 my-6 md:my-8 flex-wrap justify-center">
        <button
          v-for="tab in contentTabs"
          :key="tab.key"
          type="button"
          class="px-5 py-3 rounded-[14px] font-semibold text-[15px] transition-all duration-300 shadow-[0_4px_10px_rgba(0,0,0,0.05)]"
          :class="
            activeContentTab === tab.key
              ? 'bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white shadow-md'
              : 'bg-white text-[#6b7280] hover:bg-[#f0f4ff] hover:text-[#6366f1] hover:-translate-y-0.5'
          "
          @click="activeContentTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 内容区域 -->
      <div class="min-h-[300px]">
        <!-- 我的帖子 -->
        <div v-if="activeContentTab === 'posts'" class="space-y-4 animate-fade">
          <div class="flex flex-wrap gap-2 mb-4">
            <button v-for="sub in postSubTabs" :key="sub.key" type="button" @click="selectPostSubTab(sub.key)"
              :class="[
                'px-4 py-2 rounded-[14px] text-sm font-medium transition-all',
                postSubTab === sub.key
                  ? 'bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white'
                  : 'bg-white text-[#6b7280] hover:bg-[#f0f4ff] hover:text-[#6366f1]'
              ]">
              {{ sub.label }}
            </button>
          </div>

          <!-- 正式帖列表 -->
          <div v-if="postSubTab !== 'drafts'" class="space-y-4">
            <div v-for="(post, pidx) in myPostRows" :key="postRowId(post) || `mp-${pidx}`"
              @click="openPostRow(post)"
              class="bg-white rounded-[18px] p-5 md:p-[22px] shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] cursor-pointer">
              <div class="flex items-center gap-2 mb-2 flex-wrap">
                <h4 class="font-semibold text-[19px] text-[#1f2937] flex-1 pr-4">{{ post.title || '无标题' }}</h4>
                <span v-if="postSubTab === 'reviewing'" class="text-[10px] px-2 py-0.5 rounded-full bg-amber-100 text-amber-600 shrink-0">审核中</span>
                <span v-if="postSubTab === 'rejected'" class="text-[10px] px-2 py-0.5 rounded-full bg-red-100 text-red-600 shrink-0">违规打回</span>
              </div>
              <p class="text-[14px] text-[#6b7280] mb-3">{{ post.summary || post.content?.substring(0, 120) }}</p>
              <div class="flex items-center gap-4 text-[13px] text-[#6b7280] mb-3">
                <span>{{ formatDate(post.createTime) }}</span>
              </div>
              <div v-if="postSubTab === 'published'" class="flex flex-wrap gap-5 text-[14px] text-[#6b7280]">
                <span><i class="fas fa-eye mr-1"></i> {{ post.viewCount ?? 0 }}</span>
                <span><i class="fas fa-comment mr-1"></i> {{ post.commentCount ?? 0 }}</span>
                <span class="text-[#ec4899]"><i class="fas fa-heart mr-1"></i> {{ post.likeCount ?? 0 }}</span>
              </div>
              <div class="mt-4 flex gap-2">
                <el-button v-if="postSubTab === 'rejected'" size="small" type="warning" @click.stop="openRejectDetail(post)">打回原因</el-button>
                <el-button size="small" type="primary" @click.stop="editPost(postRowId(post))" class="!bg-[#6366f1] !border-[#6366f1]">编辑</el-button>
                <el-button size="small" type="danger" @click.stop="deletePost(postRowId(post))" class="!bg-red-500 !border-red-500">删除</el-button>
              </div>
            </div>
            <div v-if="myPostRows.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px]">
              {{ postSubTab === 'published' ? '暂无已发布帖子' : postSubTab === 'reviewing' ? '暂无审核中帖子' : '暂无违规打回记录' }}
            </div>
          </div>

          <!-- 草稿箱 -->
          <div v-else class="space-y-4">
            <div v-for="d in draftRows" :key="d.draftId"
              class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)]">
              <h4 class="font-semibold text-[19px] text-[#1f2937] mb-1">{{ d.title || '（无标题草稿）' }}</h4>
              <p class="text-[14px] text-[#6b7280] mb-2">{{ d.content || '…' }}</p>
              <div class="text-[13px] text-[#6b7280] mb-3">更新于 {{ formatDate(d.updateTime || d.createTime) }}</div>
              <div class="flex gap-2">
                <el-button size="small" type="primary" class="!bg-[#6366f1] !border-[#6366f1]" @click="continueDraft(d)">继续编辑</el-button>
                <el-button size="small" type="danger" class="!bg-red-500 !border-red-500" @click="deleteDraftRow(d)">删除</el-button>
              </div>
            </div>
            <div v-if="draftRows.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px]">
              草稿箱为空
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="postListTotal > 0" class="flex items-center justify-between pt-4">
            <span class="text-sm text-[#6b7280]">共 {{ postListTotal }} 条</span>
            <div class="flex items-center gap-2">
              <button type="button" @click="goPostListPage(postListPage - 1)" :disabled="postListPage <= 1"
                class="px-3 py-1.5 text-sm border border-[#e5e7eb] rounded-lg text-[#6b7280] disabled:opacity-40 hover:bg-[#f0f4ff] bg-white">上一页</button>
              <span class="text-sm text-[#6b7280]">{{ postListPage }} / {{ postListTotalPages }}</span>
              <button type="button" @click="goPostListPage(postListPage + 1)" :disabled="postListPage >= postListTotalPages"
                class="px-3 py-1.5 text-sm border border-[#e5e7eb] rounded-lg text-[#6b7280] disabled:opacity-40 hover:bg-[#f0f4ff] bg-white">下一页</button>
            </div>
          </div>
        </div>

        <!-- 我的喜欢 -->
        <div v-if="activeContentTab === 'likes'" class="space-y-4 animate-fade">
          <div v-for="like in likedPosts" :key="like.postId"
            @click="$router.push(`/post/${like.postId}`)"
            class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] cursor-pointer">
            <div class="flex items-center gap-2 mb-2">
              <i class="fas fa-heart text-[#ec4899]"></i>
              <span class="text-sm text-[#ec4899]">{{ formatDate(like.likeTime) }}</span>
            </div>
            <h4 class="font-semibold text-[19px] text-[#1f2937]">帖子 #{{ like.postId }}</h4>
          </div>
          <div v-if="likedPosts.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px]">
            还没有点赞过任何帖子
          </div>
          <div v-if="likedPostsTotal > 0" class="flex items-center justify-between pt-4">
            <span class="text-sm text-[#6b7280]">共 {{ likedPostsTotal }} 条</span>
            <div class="flex items-center gap-2">
              <button type="button" @click="goLikedPostsPage(likedPostsPage - 1)" :disabled="likedPostsPage <= 1"
                class="px-3 py-1.5 text-sm border border-[#e5e7eb] rounded-lg text-[#6b7280] disabled:opacity-40 hover:bg-[#f0f4ff] bg-white">上一页</button>
              <span class="text-sm text-[#6b7280]">{{ likedPostsPage }} / {{ likedPostsTotalPages }}</span>
              <button type="button" @click="goLikedPostsPage(likedPostsPage + 1)" :disabled="likedPostsPage >= likedPostsTotalPages"
                class="px-3 py-1.5 text-sm border border-[#e5e7eb] rounded-lg text-[#6b7280] disabled:opacity-40 hover:bg-[#f0f4ff] bg-white">下一页</button>
            </div>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeContentTab === 'collects'" class="space-y-4 animate-fade">
          <div v-for="col in collects" :key="col.id"
            @click="$router.push(`/post/${col.postId}`)"
            class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] cursor-pointer">
            <h4 class="font-semibold text-[19px] text-[#1f2937]">{{ col.postTitle }}</h4>
            <p class="text-[13px] text-[#6b7280] mt-2">收藏于 {{ col.collectTime }}</p>
          </div>
          <div v-if="collects.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px]">
            还没有收藏过任何帖子
          </div>
        </div>

        <!-- 我的板块 -->
        <div v-if="activeContentTab === 'sections'" class="space-y-4 animate-fade">
          <div v-for="section in mySections" :key="section.sectionId"
            @click="goSection(section)"
            class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] cursor-pointer flex items-center gap-4">
            <img :src="section.cover || 'https://picsum.photos/400/300?random=' + section.sectionId"
              class="w-16 h-16 object-cover rounded-[12px] flex-shrink-0" />
            <div class="flex-1">
              <h4 class="font-semibold text-[19px] text-[#6366f1]">📌 {{ section.name }}</h4>
              <p class="text-[14px] text-[#6b7280] mt-1">{{ section.intro || '暂无介绍' }}</p>
              <div class="text-[13px] text-[#6b7280] mt-1">
                <span>版主 · {{ section.postCount || 0 }} 帖子</span>
              </div>
            </div>
          </div>
          <div v-if="mySections.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px]">
            还没有创建任何板块，去创建第一个吧~
          </div>
        </div>

        <!-- 我加入的板块 -->
        <div v-if="activeContentTab === 'joinedSections'" class="space-y-4 animate-fade">
          <div v-for="section in joinedSections" :key="section.sectionId"
            @click="goSection(section)"
            class="bg-white rounded-[18px] p-5 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_-5px_rgba(99,102,241,0.25)] cursor-pointer flex items-center gap-4">
            <img :src="section.cover || 'https://picsum.photos/400/300?random=' + section.sectionId"
              class="w-16 h-16 object-cover rounded-[12px] flex-shrink-0" />
            <div class="flex-1">
              <h4 class="font-semibold text-[19px] text-[#1f2937]">{{ section.name }}</h4>
              <p class="text-[14px] text-[#6b7280] mt-1">{{ section.intro || '暂无介绍' }}</p>
              <div class="text-[13px] text-[#6b7280] mt-1">
                <span>成员 · {{ section.postCount || 0 }} 帖子</span>
              </div>
            </div>
          </div>
          <div v-if="joinedSections.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px]">
            还没有加入任何板块，去浏览发现感兴趣的板块吧~
          </div>
        </div>

        <!-- 签到记录 -->
        <div v-if="activeContentTab === 'checkin'" class="space-y-4 animate-fade">
          <div v-for="(record, i) in checkinRecords" :key="i"
            class="bg-white rounded-[18px] p-4 shadow-[0_10px_30px_-5px_rgba(99,102,241,0.15)] flex items-center justify-between">
            <div class="flex items-center gap-3">
              <i class="fas fa-calendar-check text-green-500"></i>
              <span class="text-[14px] text-[#1f2937]">{{ record.date }}</span>
            </div>
            <span class="text-[14px] font-semibold text-green-500">+{{ record.points }} 积分</span>
          </div>
          <div v-if="checkinRecords.length === 0" class="text-center py-12 text-[#6b7280] text-sm bg-white rounded-[18px]">
            暂无签到记录
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 弹层挂在单根内，由 Element Plus teleport 到 body；与主内容同属一个路由组件根，保证页面过渡正常 -->
  <!-- 编辑资料对话框 -->
  <el-dialog v-model="editDialogVisible" title="编辑资料" width="500px" destroy-on-close @closed="onEditDialogClosed"
      class="!rounded-[18px] overflow-hidden">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="头像">
          <div class="flex items-center gap-4">
            <img :src="resolveFileUrl(editForm.avatar) || defaultAvatar" referrerpolicy="no-referrer"
              class="w-20 h-20 rounded-full object-cover border-2 border-[#e5e7eb]" @error="onAvatarImgError" />
            <div>
              <el-button size="small" @click="triggerAvatarUpload" class="!bg-[#6366f1] !border-[#6366f1] !text-white">
                <i class="fas fa-upload mr-1"></i>选择头像
              </el-button>
              <input ref="avatarInputRef" type="file" accept="image/*" class="hidden" @change="handleAvatarUpload" />
              <p class="text-xs text-[#6b7280] mt-1">仅本地预览，点「保存」后上传到服务器</p>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" class="!rounded-[10px]" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己" class="!rounded-[10px]" />
        </el-form-item>
        <el-form-item label="个人标签">
          <div class="space-y-2">
            <div class="flex flex-wrap gap-2">
              <span v-for="(tag, idx) in editForm.bioTags" :key="idx"
                class="px-3 py-1 bg-[#f0f4ff] text-[#6366f1] rounded-full text-sm flex items-center gap-1">
                {{ tag }}
                <button type="button" @click="removeEditTag(idx)" class="text-[#6366f1] hover:text-[#4f46e5]">×</button>
              </span>
            </div>
            <el-input v-model="newTagInput" placeholder="输入标签按回车添加（最多10个）" class="!rounded-[10px]"
              @keyup.enter="addEditTag" />
            <p class="text-xs text-[#6b7280]">标签用于展示你的兴趣和特长</p>
          </div>
        </el-form-item>
        <el-form-item label="隐私设置">
          <div class="space-y-2">
            <el-checkbox v-model="editForm.hideLikes" :true-value="1" :false-value="0">
              隐藏我的喜欢列表
            </el-checkbox>
            <el-checkbox v-model="editForm.hideCollects" :true-value="1" :false-value="0">
              隐藏我的收藏列表
            </el-checkbox>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false" class="!rounded-[10px]">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="saving" class="!bg-[#6366f1] !border-[#6366f1] !rounded-[10px]">保存</el-button>
      </template>
    </el-dialog>

    <!-- 创建板块对话框 -->
    <el-dialog v-model="sectionDialogVisible" title="创建板块" width="520px" destroy-on-close class="!rounded-[18px]">
      <el-form :model="sectionForm" label-width="90px">
        <el-form-item label="板块名称">
          <el-input v-model="sectionForm.name" placeholder="请输入板块名称" class="!rounded-[10px]" />
        </el-form-item>
        <el-form-item label="板块介绍">
          <el-input v-model="sectionForm.intro" type="textarea" :rows="3" placeholder="介绍一下板块" class="!rounded-[10px]" />
        </el-form-item>

        <!-- 分类选择 -->
        <el-form-item label="板块分类">
          <div class="space-y-3">
            <!-- 切换使用现有分类或自定义 -->
            <el-radio-group v-model="useCustomCategory" class="flex gap-4">
              <el-radio :label="false">选择现有分类</el-radio>
              <el-radio :label="true">自定义分类</el-radio>
            </el-radio-group>

            <!-- 现有分类下拉框 -->
            <el-select
              v-if="!useCustomCategory"
              v-model="sectionForm.categoryId"
              placeholder="请选择分类"
              class="w-full !rounded-[10px]"
              clearable
            >
              <el-option
                v-for="cat in sectionCategories"
                :key="cat.categoryId"
                :label="cat.name"
                :value="cat.categoryId"
              />
            </el-select>

            <!-- 自定义分类输入 -->
            <el-input
              v-else
              v-model="sectionForm.customCategory"
              placeholder="请输入自定义分类名称（如：摄影、美食、旅行等）"
              class="!rounded-[10px]"
              maxlength="20"
              show-word-limit
            />
          </div>
        </el-form-item>

        <el-form-item label="板块状态">
          <el-select v-model="sectionForm.status" placeholder="请选择状态" class="w-full !rounded-[10px]">
            <el-option label="公开" :value="1" />
            <el-option label="私密" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sectionDialogVisible = false" class="!rounded-[10px]">取消</el-button>
        <el-button type="primary" @click="createSection" :loading="creatingSection" class="!bg-[#6366f1] !border-[#6366f1] !rounded-[10px]">创建</el-button>
      </template>
    </el-dialog>

    <!-- 关注列表弹窗 -->
    <el-dialog v-model="showFollowingDialog" title="我的关注" width="500px" class="!rounded-[18px]">
      <div class="space-y-3 max-h-[400px] overflow-y-auto">
        <div v-for="user in followingList" :key="user.userId"
          @click="$router.push(`/user/${user.userId}`)"
          class="flex items-center justify-between p-3 rounded-[14px] hover:bg-[#f0f4ff] cursor-pointer border border-[#e5e7eb] transition-all">
          <div class="flex items-center gap-3">
            <img :src="resolveFileUrl(user.avatar) || defaultAvatar" class="w-10 h-10 rounded-full object-cover" />
            <div>
              <div class="font-semibold text-[#1f2937]">{{ user.nickname || user.username }}</div>
              <div class="text-xs text-[#6b7280]">@{{ user.username }}</div>
            </div>
          </div>
          <el-button type="primary" size="small" @click.stop="handleUnfollow(user.userId)" class="!bg-[#6366f1] !border-[#6366f1] !rounded-[10px]">取消关注</el-button>
        </div>
        <div v-if="followingList.length === 0" class="text-center py-8 text-[#6b7280] text-sm">
          暂无关注
        </div>
      </div>
    </el-dialog>

    <!-- 违规打回弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="违规打回说明" width="520px" class="!rounded-[18px]">
      <p class="text-sm text-[#6b7280] mb-2">帖子：{{ rejectDialogTitle }}</p>
      <div class="rounded-[14px] bg-red-50 border border-red-200 p-4 text-sm text-red-700 whitespace-pre-wrap">{{ rejectDialogReason }}</div>
      <template #footer>
        <el-button type="primary" @click="rejectDialogVisible = false" class="!bg-[#6366f1] !border-[#6366f1] !rounded-[10px]">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 粉丝列表弹窗 -->
    <el-dialog v-model="showFollowersDialog" title="我的粉丝" width="500px" class="!rounded-[18px]">
      <div class="space-y-3 max-h-[400px] overflow-y-auto">
        <div v-for="user in followersList" :key="user.userId"
          @click="$router.push(`/user/${user.userId}`)"
          class="flex items-center justify-between p-3 rounded-[14px] hover:bg-[#f0f4ff] cursor-pointer border border-[#e5e7eb] transition-all">
          <div class="flex items-center gap-3">
            <img :src="resolveFileUrl(user.avatar) || defaultAvatar" class="w-10 h-10 rounded-full object-cover" />
            <div>
              <div class="font-semibold text-[#1f2937]">{{ user.nickname || user.username }}</div>
              <div class="text-xs text-[#6b7280]">@{{ user.username }}</div>
            </div>
          </div>
        </div>
        <div v-if="followersList.length === 0" class="text-center py-8 text-[#6b7280] text-sm">
          暂无粉丝
        </div>
      </div>
  </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi, postApi, interactApi, fileApi, sectionApi } from '../../../api'
import { postRowId } from '../../../utils/postRowId'
import type { UserInfo, Section } from '../../../types'
import { resolveFileUrl } from '../../../utils/resolveFileUrl'

const router = useRouter()

const checkining = ref(false)
const activeContentTab = ref('posts')
const editDialogVisible = ref(false)
const saving = ref(false)
const avatarInputRef = ref<HTMLInputElement>()
const avatarFilePending = ref<File | null>(null)
const avatarPreviewObjectUrl = ref<string | null>(null)
const showFollowingDialog = ref(false)
const showFollowersDialog = ref(false)
const followingList = ref<any[]>([])
const followersList = ref<any[]>([])

const editForm = reactive({
  nickname: '',
  bio: '',
  avatar: '',
  bioTags: [] as string[],
  hideLikes: 0,
  hideCollects: 0,
})

const newTagInput = ref('')

function addEditTag() {
  const tag = newTagInput.value.trim()
  if (!tag) return
  if (editForm.bioTags.length >= 10) {
    ElMessage.warning('最多只能添加10个标签')
    return
  }
  if (!editForm.bioTags.includes(tag)) {
    editForm.bioTags.push(tag)
  }
  newTagInput.value = ''
}

function removeEditTag(idx: number) {
  editForm.bioTags.splice(idx, 1)
}

function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

function onAvatarImgError(e: Event) {
  const el = e.target as HTMLImageElement
  if (el && el.src && !el.src.includes('placehold.co')) {
    el.src = defaultAvatar
  }
}

function revokeAvatarPreview() {
  if (avatarPreviewObjectUrl.value) {
    URL.revokeObjectURL(avatarPreviewObjectUrl.value)
    avatarPreviewObjectUrl.value = null
  }
  avatarFilePending.value = null
}

function handleAvatarUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  revokeAvatarPreview()
  avatarFilePending.value = file
  const objectUrl = URL.createObjectURL(file)
  avatarPreviewObjectUrl.value = objectUrl
  editForm.avatar = objectUrl
  input.value = ''
  ElMessage.info('头像已本地预览，保存后上传')
}

function showEditDialog() {
  revokeAvatarPreview()
  editForm.nickname = userInfo.nickname || ''
  editForm.bio = userInfo.bio || ''
  editForm.avatar = userInfo.avatar || ''
  editForm.bioTags = parsedBioTags.value
  editForm.hideLikes = userInfo.hideLikes || 0
  editForm.hideCollects = userInfo.hideCollects || 0
  editDialogVisible.value = true
}

function onEditDialogClosed() {
  revokeAvatarPreview()
  editForm.avatar = userInfo.avatar || ''
}

async function saveProfile() {
  saving.value = true
  try {
    let avatarToSave = editForm.avatar
    if (avatarFilePending.value) {
      const formData = new FormData()
      formData.append('file', avatarFilePending.value)
      const res = (await fileApi.upload(
        formData,
        undefined,
        'community/images/avatar',
        'avatar'
      )) as { id?: string }
      if (res == null || res.id == null || String(res.id).trim() === '') {
        ElMessage.error('头像上传失败，请重试')
        return
      }
      avatarToSave = String(res.id).trim()
    }
    await userApi.update({
      nickname: editForm.nickname,
      bio: editForm.bio,
      avatar: avatarToSave,
      bioTags: editForm.bioTags.join(','),
      hideLikes: editForm.hideLikes,
      hideCollects: editForm.hideCollects,
    }) as any
    revokeAvatarPreview()
    userInfo.nickname = editForm.nickname
    userInfo.bio = editForm.bio
    userInfo.avatar = avatarToSave
    userInfo.bioTags = editForm.bioTags.join(',')
    userInfo.hideLikes = editForm.hideLikes
    userInfo.hideCollects = editForm.hideCollects
    editForm.avatar = avatarToSave
    editDialogVisible.value = false
    ElMessage.success('资料更新成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const userInfo = reactive<UserInfo>({
  id: 0,
  username: '',
  nickname: '',
  avatar: '',
  bio: '',
  followersCount: 0,
  followingCount: 0,
  postsCount: 0,
  points: 0,
  likeCount: 0,
  hideLikes: 0,
  hideCollects: 0,
  bioTags: '',
})

const checkinStatus = reactive({
  checkedToday: false,
  continuousDays: 0,
  totalDays: 0,
})

const postSubTab = ref<'published' | 'reviewing' | 'rejected' | 'drafts'>('published')
const postSubTabs = [
  { key: 'published' as const, label: '已发布' },
  { key: 'reviewing' as const, label: '审核中' },
  { key: 'rejected' as const, label: '违规打回' },
  { key: 'drafts' as const, label: '草稿箱' },
]
const myPostRows = ref<any[]>([])
const draftRows = ref<any[]>([])
const postListPage = ref(1)
const postListPageSize = 10
const postListTotal = ref(0)
const rejectDialogVisible = ref(false)
const rejectDialogTitle = ref('')
const rejectDialogReason = ref('')

const postListTotalPages = computed(() => Math.max(1, Math.ceil(postListTotal.value / postListPageSize)))
const collects = ref<any[]>([])
const likedPosts = ref<any[]>([])
const likedPostsPage = ref(1)
const likedPostsPageSize = 10
const likedPostsTotal = ref(0)
const likedPostsTotalPages = computed(() => Math.max(1, Math.ceil(likedPostsTotal.value / likedPostsPageSize)))
const checkinRecords = ref<any[]>([])
const mySections = ref<Section[]>([])
const joinedSections = ref<Section[]>([])
const sectionDialogVisible = ref(false)
const sectionForm = reactive({
  name: '',
  intro: '',
  icon: '',
  status: 1,
  categoryId: null as number | null,
  customCategory: ''
})
const creatingSection = ref(false)
const sectionCategories = ref<{ categoryId: number; name: string; sort: number }[]>([])
const useCustomCategory = ref(false)

const defaultAvatar = 'https://placehold.co/112x112/E8EAF6/9FA8DA?text=U'

const level = computed(() => {
  const pts = userInfo.points || 0
  if (pts < 100) return 1
  if (pts < 300) return 2
  if (pts < 600) return 3
  if (pts < 1000) return 4
  return 5
})

const contentTabs = [
  { key: 'posts', label: '我的帖子' },
  { key: 'likes', label: '我的喜欢' },
  { key: 'collects', label: '我的收藏' },
  { key: 'sections', label: '我的板块' },
  { key: 'joinedSections', label: '我加入的板块' },
  { key: 'checkin', label: '签到记录' }
]

// 解析个人标签
const parsedBioTags = computed(() => {
  if (!userInfo.bioTags) return []
  return userInfo.bioTags.split(',').map(t => t.trim()).filter(t => t)
})

function focusMyPosts() {
  activeContentTab.value = 'posts'
  postSubTab.value = 'published'
  postListPage.value = 1
  loadMyPostList()
}

function selectPostSubTab(key: typeof postSubTab.value) {
  postSubTab.value = key
  postListPage.value = 1
  loadMyPostList()
}

function goPostListPage(p: number) {
  if (p < 1 || p > postListTotalPages.value) return
  postListPage.value = p
  loadMyPostList()
}

async function loadMyPostList() {
  if (activeContentTab.value !== 'posts') return
  try {
    if (postSubTab.value === 'drafts') {
      const res: any = await postApi.drafts(postListPage.value, postListPageSize)
      draftRows.value = res?.records || []
      myPostRows.value = []
      postListTotal.value = Number(res?.total ?? 0)
    } else {
      const st = postSubTab.value === 'published' ? 1 : postSubTab.value === 'reviewing' ? 2 : 3
      const res: any = await postApi.my(postListPage.value, postListPageSize, st)
      myPostRows.value = res?.records || []
      draftRows.value = []
      postListTotal.value = Number(res?.total ?? 0)
    }
  } catch {
    myPostRows.value = []
    draftRows.value = []
    postListTotal.value = 0
  }
}

function openPostRow(post: any) {
  const id = postRowId(post)
  if (id == null) return
  router.push(`/post/${id}`)
}

function openRejectDetail(post: any) {
  rejectDialogTitle.value = post.title || '无标题'
  const r = post.rejectReason
  rejectDialogReason.value = r != null && String(r).trim() !== '' ? String(r) : '未填写具体原因，如对处理有疑问请联系管理员。'
  rejectDialogVisible.value = true
}

function continueDraft(d: any) {
  const id = d.draftId
  if (id == null) return
  router.push({ path: '/post/create', query: { draftId: String(id) } })
}

async function deleteDraftRow(d: any) {
  const id = d.draftId
  if (id == null) return
  try {
    await ElMessageBox.confirm('确定删除该草稿？', '删除草稿', { type: 'warning' })
    await postApi.deleteDraft(id)
    ElMessage.success('已删除')
    loadMyPostList()
  } catch (e: any) {
    if (e !== 'cancel') {
      /* 拦截器已提示 */
    }
  }
}

watch([activeContentTab, postSubTab], () => {
  if (activeContentTab.value === 'posts') {
    loadMyPostList()
  } else if (activeContentTab.value === 'likes') {
    loadLikedPosts()
  }
})

async function loadLikedPosts() {
  if (activeContentTab.value !== 'likes') return
  try {
    const res: any = await userApi.getMyLikedPosts(likedPostsPage.value, likedPostsPageSize)
    likedPosts.value = res?.records || []
    likedPostsTotal.value = Number(res?.total ?? 0)
  } catch {
    likedPosts.value = []
    likedPostsTotal.value = 0
  }
}

function goLikedPostsPage(p: number) {
  if (p < 1 || p > likedPostsTotalPages.value) return
  likedPostsPage.value = p
  loadLikedPosts()
}

async function doCheckin() {
  if (checkinStatus.checkedToday) return
  checkining.value = true
  try {
    const res = await userApi.checkin() as any
    ElMessage.success(`签到成功！获得${res.points || 5}积分`)
    checkinStatus.checkedToday = true
    checkinStatus.continuousDays++
    checkinStatus.totalDays++
    userInfo.points += res.points || 5
  } catch (e: any) {
    ElMessage.error(e?.message || '签到失败')
  } finally {
    checkining.value = false
  }
}

async function loadFollowing() {
  try {
    const res = await userApi.following(userInfo.id) as any
    followingList.value = Array.isArray(res) ? res : []
    showFollowingDialog.value = true
  } catch (e) {
    console.error('加载关注列表失败', e)
    ElMessage.error('加载关注列表失败')
  }
}

async function loadFollowers() {
  try {
    const res = await userApi.followers(userInfo.id) as any
    followersList.value = Array.isArray(res) ? res : []
    showFollowersDialog.value = true
  } catch (e) {
    console.error('加载粉丝列表失败', e)
    ElMessage.error('加载粉丝列表失败')
  }
}

async function handleUnfollow(userId: number) {
  try {
    await userApi.unfollow(userId)
    ElMessage.success('已取消关注')
    followingList.value = followingList.value.filter(u => u.id !== userId)
    userInfo.followingCount = Math.max(0, (userInfo.followingCount || 0) - 1)
  } catch (e) {
    ElMessage.error('取消关注失败')
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleDateString('zh-CN')
}

onMounted(async () => {
  const token = localStorage.getItem('accessToken')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    const [infoRes, statusRes]: any[] = await Promise.all([userApi.my(), userApi.checkinStatus()])
    Object.assign(userInfo, infoRes || {})
    if (statusRes) {
      const s = statusRes as any
      checkinStatus.checkedToday = !!s.checkedToday
      checkinStatus.continuousDays = s.continuousDays || 0
      checkinStatus.totalDays = s.totalDays || 0
    }
    await loadMyPostList()
    await loadLikedPosts()
    loadCheckinRecords()
  } catch (e: any) {
    console.error('获取用户信息失败', e)
    if (e?.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
    }
    return
  }

  try {
    const collectRes: any = await interactApi.collects(1, 20)
    collects.value = (collectRes?.records || []).map((c: any) => ({
      ...c,
      id: c.id || c.postId,
      postId: c.postId,
      postTitle: c.postTitle && String(c.postTitle).trim() ? c.postTitle : '未命名帖子',
      collectTime: c.createTime ? formatDate(c.createTime) : '',
    }))
  } catch (e) {
    console.error('获取收藏列表失败', e)
  }

  try {
    const sectionsRes: any = await sectionApi.userSections()
    mySections.value = sectionsRes || []
  } catch (e) {
    console.error('获取我的板块失败', e)
  }

  try {
    const joinedRes: any = await sectionApi.joinedSections()
    joinedSections.value = Array.isArray(joinedRes) ? joinedRes : []
  } catch (e) {
    joinedSections.value = []
    console.error('获取我加入的板块失败', e)
  }
})

async function loadCheckinRecords() {
  try {
    checkinRecords.value = []
    const today = new Date()
    for (let i = checkinStatus.continuousDays - 1; i >= 0; i--) {
      const date = new Date(today)
      date.setDate(date.getDate() - i)
      checkinRecords.value.push({
        date: date.toLocaleDateString('zh-CN'),
        points: 5
      })
    }
  } catch (e) {
    console.error('获取签到记录失败', e)
  }
}

async function showCreateSectionDialog() {
  sectionForm.name = ''
  sectionForm.intro = ''
  sectionForm.icon = ''
  sectionForm.status = 1
  sectionForm.categoryId = null
  sectionForm.customCategory = ''
  useCustomCategory.value = false

  // 加载分类列表
  try {
    const res: any = await sectionApi.categories()
    sectionCategories.value = res.data || []
  } catch (e) {
    sectionCategories.value = []
  }

  sectionDialogVisible.value = true
}

async function createSection() {
  if (!sectionForm.name) {
    ElMessage.warning('请输入板块名称')
    return
  }

  // 验证分类选择
  if (useCustomCategory.value && !sectionForm.customCategory?.trim()) {
    ElMessage.warning('请输入自定义分类名称')
    return
  }

  creatingSection.value = true
  try {
    const submitData = {
      name: sectionForm.name,
      intro: sectionForm.intro,
      icon: sectionForm.icon,
      status: sectionForm.status,
      categoryId: useCustomCategory.value ? null : sectionForm.categoryId,
      customCategory: useCustomCategory.value ? sectionForm.customCategory.trim() : ''
    }
    await sectionApi.create(submitData)
    ElMessage.success('板块创建成功')
    sectionDialogVisible.value = false
    const sectionsRes: any = await sectionApi.userSections()
    mySections.value = sectionsRes || []
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  } finally {
    creatingSection.value = false
  }
}

function goSection(section: Section) {
  const sid = section.sectionId ?? section.id
  if (sid == null) return
  router.push(`/section/${sid}`)
}

async function editPost(postId: number) {
  router.push(`/post/create?edit=${postId}`)
}

async function deletePost(postId: number) {
  ElMessageBox.confirm(
    '确定要删除这篇帖子吗？',
    '删除帖子',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await postApi.delete(postId)
        ElMessage.success('删除成功')
        await loadMyPostList()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 取消删除
    })
}
</script>

<style scoped>
/* 添加淡入动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade {
  animation: fadeIn 0.5s ease;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f0f4ff;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c7d2fe;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #818cf8;
}
</style>